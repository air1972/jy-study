package com.jy.study.framework.shiro.service;

import java.util.List;
import java.util.Set;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.jy.study.common.constant.Constants;
import com.jy.study.common.constant.ShiroConstants;
import com.jy.study.common.constant.UserConstants;
import com.jy.study.common.core.domain.entity.SysRole;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.enums.UserStatus;
import com.jy.study.common.exception.user.BlackListException;
import com.jy.study.common.exception.user.CaptchaException;
import com.jy.study.common.exception.user.UserBlockedException;
import com.jy.study.common.exception.user.UserDeleteException;
import com.jy.study.common.exception.user.UserNotExistsException;
import com.jy.study.common.exception.user.UserPasswordNotMatchException;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.IpUtils;
import com.jy.study.common.utils.MessageUtils;
import com.jy.study.common.utils.ServletUtils;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.framework.manager.AsyncManager;
import com.jy.study.framework.manager.factory.AsyncFactory;
import com.jy.study.system.service.ISysConfigService;
import com.jy.study.system.service.ISysMenuService;
import com.jy.study.system.service.ISysUserService;

/**
 * 登录校验方法
 * 
 * @author jily
 */
@Component
public class SysLoginService
{
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录
     */
    public SysUser login(String username, String password)
    {
        // 验证码校验
        if (ShiroConstants.CAPTCHA_ERROR.equals(ServletUtils.getRequest().getAttribute(ShiroConstants.CURRENT_CAPTCHA)))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }

        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, ShiroUtils.getIp()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }

        // 查询用户信息
        SysUser user = userService.selectUserByLoginName(username);

        /**
        if (user == null && maybeMobilePhoneNumber(username))
        {
            user = userService.selectUserByPhoneNumber(username);
        }

        if (user == null && maybeEmail(username))
        {
            user = userService.selectUserByEmail(username);
        }
        */

        if (user == null)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new UserNotExistsException();
        }
        
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.delete")));
            throw new UserDeleteException();
        }
        
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.blocked")));
            throw new UserBlockedException();
        }

        // 普通注册用户：试用/会员到期后自动停用
        if (UserConstants.REGISTER_USER_TYPE.equals(user.getUserType()))
        {
            Date expire = user.getVipExpireTime();
            if (expire == null || expire.before(new Date()))
            {
                SysUser update = new SysUser();
                update.setUserId(user.getUserId());
                update.setStatus(UserConstants.USER_DISABLE);
                update.setUpdateBy("system");
                userService.updateUserInfo(update);
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "试用已到期，请充值续费"));
                throw new UserBlockedException();
            }
        }

        passwordService.validate(user, password);

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        setRolePermission(user);
        recordLoginInfo(user.getUserId());
        return user;
    }

    /**
    private boolean maybeEmail(String username)
    {
        if (!username.matches(UserConstants.EMAIL_PATTERN))
        {
            return false;
        }
        return true;
    }

    private boolean maybeMobilePhoneNumber(String username)
    {
        if (!username.matches(UserConstants.MOBILE_PHONE_NUMBER_PATTERN))
        {
            return false;
        }
        return true;
    }
    */

    /**
     * 设置角色权限
     *
     * @param user 用户信息
     */
    public void setRolePermission(SysUser user)
    {
        List<SysRole> roles = user.getRoles();
        if (!roles.isEmpty() && roles.size() > 1)
        {
            // 多角色设置permissions属性，以便数据权限匹配权限
            for (SysRole role : roles)
            {
                Set<String> rolePerms = menuService.selectPermsByRoleId(role.getRoleId());
                role.setPermissions(rolePerms);
            }
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setLoginIp(ShiroUtils.getIp());
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserInfo(user);
    }
}
