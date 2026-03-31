package com.jy.study.web.controller.system;

import com.jy.study.common.annotation.Log;
import com.jy.study.common.constant.UserConstants;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.system.service.ISysUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 会员管理
 *
 * @author jily
 */
@Controller
@RequestMapping("/system/member")
public class SysMemberController extends BaseController
{
    private final String prefix = "system/member";

    @Autowired
    private ISysUserService userService;

    @RequiresPermissions("system:member:view")
    @GetMapping()
    public String member()
    {
        return prefix + "/member";
    }

    @RequiresPermissions("system:member:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysUser user)
    {
        user.setUserType(UserConstants.REGISTER_USER_TYPE);
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @RequiresPermissions("system:member:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        SysUser user = userService.selectUserById(userId);
        if (user == null || !StringUtils.equals(UserConstants.REGISTER_USER_TYPE, user.getUserType()))
        {
            mmap.put("user", new SysUser());
            mmap.put("errorMsg", "该用户不是会员用户，无法编辑。");
            return prefix + "/edit";
        }
        mmap.put("user", user);
        return prefix + "/edit";
    }

    @RequiresPermissions("system:member:edit")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(SysUser user)
    {
        if (user.getUserId() == null)
        {
            return error("用户ID不能为空");
        }
        userService.checkUserDataScope(user.getUserId());
        SysUser dbUser = userService.selectUserById(user.getUserId());
        if (dbUser == null)
        {
            return error("用户不存在");
        }
        if (!StringUtils.equals(UserConstants.REGISTER_USER_TYPE, dbUser.getUserType()))
        {
            return error("仅允许修改会员用户");
        }
        SysUser update = new SysUser();
        update.setUserId(user.getUserId());
        update.setVipExpireTime(user.getVipExpireTime());
        update.setStatus(user.getStatus());
        update.setUpdateBy(getLoginName());
        return toAjax(userService.updateUserInfo(update));
    }
}

