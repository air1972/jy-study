package com.jy.study.web.controller.system;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.Ztree;
import com.jy.study.common.core.domain.entity.SysDept;
import com.jy.study.common.core.domain.entity.SysRole;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.core.text.Convert;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.framework.shiro.service.SysPasswordService;
import com.jy.study.framework.shiro.util.AuthorizationUtils;
import com.jy.study.system.service.ISysConfigService;
import com.jy.study.system.service.ISysDeptService;
import com.jy.study.system.service.ISysPostService;
import com.jy.study.system.service.ISysRoleService;
import com.jy.study.system.service.ISysUserService;

/**
 * 用户信息
 * 
 * @author jily
 */
@Controller
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    private String prefix = "system/user";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;
    
    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ISysConfigService configService;

    @RequiresPermissions("system:user:view")
    @GetMapping()
    public String user()
    {
        return prefix + "/user";
    }

    @RequiresPermissions("system:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:user:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.exportExcel(list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @RequiresPermissions("system:user:import")
    @PostMapping("/importData")
    @ResponseBody
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String message = userService.importUser(userList, updateSupport, getLoginName());
        return AjaxResult.success(message);
    }

    @RequiresPermissions("system:user:view")
    @GetMapping("/importTemplate")
    @ResponseBody
    public AjaxResult importTemplate()
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        return util.importTemplateExcel("用户数据");
    }

    /**
     * 新增用户
     */
    @GetMapping("/add")
    public String add(ModelMap mmap)
    {
        mmap.put("roles", roleService.selectRoleAll().stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        mmap.put("posts", postService.selectPostAll());
        return prefix + "/add";
    }

    /**
     * 新增保存用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysUser user)
    {
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("新增用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setCreateBy(getLoginName());
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        mmap.put("user", userService.selectUserById(userId));
        mmap.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        mmap.put("posts", postService.selectPostsByUserId(userId));
        return prefix + "/edit";
    }

    /**
     * 查询用户详细
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/view/{userId}")
    public String view(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        SysUser user = userService.selectUserById(userId);
        mmap.put("user", user);
        mmap.put("roleGroup", userService.selectUserRoleGroup(userId));
        mmap.put("postGroup", userService.selectUserPostGroup(userId));
        appendPasswordMeta(user, mmap);
        return prefix + "/view";
    }

    /**
     * 修改保存用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        deptService.checkDeptDataScope(user.getDeptId());
        roleService.checkRoleDataScope(user.getRoleIds());
        if (!userService.checkLoginNameUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getLoginName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getLoginName());
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return toAjax(userService.updateUser(user));
    }

    @RequiresPermissions("system:user:resetPwd")
    @GetMapping("/resetPwd/{userId}")
    public String resetPwd(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        userService.checkUserDataScope(userId);
        SysUser user = userService.selectUserById(userId);
        mmap.put("user", user);
        appendPasswordMeta(user, mmap);
        return prefix + "/resetPwd";
    }

    @RequiresPermissions("system:user:resetPwd")
    @GetMapping("/batchResetPwd")
    public String batchResetPwd(String ids, ModelMap mmap)
    {
        Long[] userIds = Convert.toLongArray(ids);
        long skippedAdminCount = Arrays.stream(userIds).filter(SysUser::isAdmin).count();
        List<SysUser> users = Arrays.stream(userIds)
                .filter(userId -> !SysUser.isAdmin(userId))
                .peek(userService::checkUserDataScope)
                .map(userService::selectUserById)
                .filter(StringUtils::isNotNull)
                .collect(Collectors.toList());
        mmap.put("ids", users.stream().map(item -> String.valueOf(item.getUserId())).collect(Collectors.joining(",")));
        mmap.put("users", users);
        mmap.put("selectedCount", users.size());
        mmap.put("skippedAdminCount", skippedAdminCount);
        mmap.put("initPassword", configService.selectConfigByKey("sys.user.initPassword"));
        return prefix + "/batchResetPwd";
    }

    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/resetPwd")
    @ResponseBody
    public AjaxResult resetPwdSave(SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setSalt(ShiroUtils.randomSalt());
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), user.getPassword(), user.getSalt()));
        user.setPwdUpdateDate(DateUtils.getNowDate());
        user.setUpdateBy(getLoginName());
        if (userService.resetUserPwd(user) > 0)
        {
            if (ShiroUtils.getUserId().longValue() == user.getUserId().longValue())
            {
                setSysUser(userService.selectUserById(user.getUserId()));
            }
            return success();
        }
        return error();
    }

    @RequiresPermissions("system:user:resetPwd")
    @Log(title = "批量重置密码", businessType = BusinessType.UPDATE)
    @PostMapping("/batchResetPwd")
    @ResponseBody
    public AjaxResult batchResetPwdSave(String ids, String password)
    {
        Long[] userIds = Convert.toLongArray(ids);
        if (userIds == null || userIds.length == 0)
        {
            return error("请至少选择一个用户");
        }
        long skippedAdminCount = Arrays.stream(userIds).filter(SysUser::isAdmin).count();
        int rows = userService.batchResetUserPwd(userIds, password, getLoginName());
        if (rows > 0)
        {
            if (skippedAdminCount > 0)
            {
                return success("已统一修改 " + rows + " 个用户的密码，超级管理员已自动跳过");
            }
            return success("已统一修改 " + rows + " 个用户的密码");
        }
        if (skippedAdminCount > 0)
        {
            return AjaxResult.warn("本次选择的用户仅包含超级管理员，未执行批量改密");
        }
        return error("未修改任何用户密码");
    }

    /**
     * 进入授权角色页
     */
    @GetMapping("/authRole/{userId}")
    public String authRole(@PathVariable("userId") Long userId, ModelMap mmap)
    {
        SysUser user = userService.selectUserById(userId);
        // 获取用户所属的角色列表
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        mmap.put("user", user);
        mmap.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return prefix + "/authRole";
    }

    /**
     * 用户授权角色
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PostMapping("/authRole/insertAuthRole")
    @ResponseBody
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        roleService.checkRoleDataScope(roleIds);
        userService.insertUserAuth(userId, roleIds);
        AuthorizationUtils.clearAllCachedAuthorizationInfo();
        return success();
    }

    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        if (ArrayUtils.contains(Convert.toLongArray(ids), getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(ids));
    }

    /**
     * 校验用户名
     */
    @PostMapping("/checkLoginNameUnique")
    @ResponseBody
    public boolean checkLoginNameUnique(SysUser user)
    {
        return userService.checkLoginNameUnique(user);
    }

    /**
     * 校验手机号码
     */
    @PostMapping("/checkPhoneUnique")
    @ResponseBody
    public boolean checkPhoneUnique(SysUser user)
    {
        return userService.checkPhoneUnique(user);
    }

    /**
     * 校验email邮箱
     */
    @PostMapping("/checkEmailUnique")
    @ResponseBody
    public boolean checkEmailUnique(SysUser user)
    {
        return userService.checkEmailUnique(user);
    }

    /**
     * 用户状态修改
     */
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.changeStatus(user));
    }

    /**
     * 加载部门列表树
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/deptTreeData")
    @ResponseBody
    public List<Ztree> deptTreeData()
    {
        List<Ztree> ztrees = deptService.selectDeptTree(new SysDept());
        return ztrees;
    }

    /**
     * 选择部门树
     * 
     * @param deptId 部门ID
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/selectDeptTree/{deptId}")
    public String selectDeptTree(@PathVariable("deptId") Long deptId, ModelMap mmap)
    {
        mmap.put("dept", deptService.selectDeptById(deptId));
        return prefix + "/deptTree";
    }

    private void appendPasswordMeta(SysUser user, ModelMap mmap)
    {
        String initPassword = StringUtils.defaultString(configService.selectConfigByKey("sys.user.initPassword"));
        boolean canDisplayRawPassword = false;
        String currentPasswordDisplay = "系统仅保存加密后的密码，当前原始密码无法直接查看，可为该用户重新设置新密码。";
        String passwordStatusText = "已设置自定义密码";
        String passwordStatusClass = "label-primary";

        if (user != null && StringUtils.isNotEmpty(user.getPassword()) && StringUtils.isNotEmpty(user.getSalt())
                && StringUtils.isNotEmpty(user.getLoginName()) && StringUtils.isNotEmpty(initPassword))
        {
            String encryptedInitPassword = passwordService.encryptPassword(user.getLoginName(), initPassword, user.getSalt());
            if (StringUtils.equals(encryptedInitPassword, user.getPassword()))
            {
                canDisplayRawPassword = true;
                currentPasswordDisplay = initPassword;
                passwordStatusText = "当前仍使用初始密码";
                passwordStatusClass = "label-warning";
            }
        }

        if (user != null && user.getPassword() == null)
        {
            passwordStatusText = "未设置密码";
            passwordStatusClass = "label-default";
            currentPasswordDisplay = "该用户当前未设置登录密码。";
        }

        mmap.put("canDisplayRawPassword", canDisplayRawPassword);
        mmap.put("currentPasswordDisplay", currentPasswordDisplay);
        mmap.put("passwordStatusText", passwordStatusText);
        mmap.put("passwordStatusClass", passwordStatusClass);
        mmap.put("passwordDigestPreview", maskPasswordDigest(user == null ? null : user.getPassword()));
        mmap.put("initPassword", initPassword);
    }

    private String maskPasswordDigest(String passwordDigest)
    {
        if (StringUtils.isEmpty(passwordDigest))
        {
            return "未记录";
        }
        if (passwordDigest.length() <= 12)
        {
            return passwordDigest;
        }
        return passwordDigest.substring(0, 6) + "..." + passwordDigest.substring(passwordDigest.length() - 4);
    }
}
