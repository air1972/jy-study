package com.jy.study.web.controller.web;

import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.ossfile.OssClientUtil;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.framework.shiro.service.SysPasswordService;
import com.jy.study.lesson.domain.LearningAnalysis;
import com.jy.study.lesson.domain.LearningPath;
import com.jy.study.lesson.service.ILearningAnalysisService;
import com.jy.study.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/web/user/profile")
public class WebProfileController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(WebProfileController.class);

    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private ILearningAnalysisService learningAnalysisService;

    /**
     * 个人中心页面
     */
    @GetMapping()
    public String profile(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);
        return "web/user/profile";
    }

    /**
     * 修改用户信息
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    @ResponseBody
    public AjaxResult updateProfile(SysUser user) {
        SysUser currentUser = getSysUser();
        currentUser.setUserName(user.getUserName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (userService.updateUserInfo(currentUser) > 0) {
            setSysUser(userService.selectUserById(currentUser.getUserId()));
            return success();
        }
        return error();
    }

    /**
     * 修改密码
     */
    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePwd")
    @ResponseBody
    public AjaxResult updatePwd(String oldPassword, String newPassword) {
        SysUser user = getSysUser();
        if (!passwordService.matches(user, oldPassword)) {
            return error("修改密码失败，原密码错误");
        }
        if (passwordService.matches(user, newPassword)) {
            return error("新密码不能与原密码相同");
        }
        
        // 更新用户密码信息
        user.setSalt(ShiroUtils.randomSalt());  // 生成新的随机盐值
        user.setPassword(passwordService.encryptPassword(user.getLoginName(), newPassword, user.getSalt()));  // 加密新密码
        user.setPwdUpdateDate(DateUtils.getNowDate());

        if (userService.resetUserPwd(user) > 0) {
            setSysUser(userService.selectUserById(user.getUserId()));
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 更新用户头像，包括上传和更新数据库
     */
    @Log(title = "个人头像", businessType = BusinessType.UPDATE)
    @PostMapping("/updateAvatar")
    @ResponseBody
    public AjaxResult updateAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("请选择要上传的图片");
        }
        try {
            String imageUrl = OssClientUtil.uploadImage(file,"avatar");
            if(StringUtils.isBlank(imageUrl)){
                return AjaxResult.error("上传失败");
            }
            SysUser user = getSysUser();
            user.setAvatar(imageUrl);
            if (userService.updateUserInfo(user) > 0) {
                setSysUser(userService.selectUserById(user.getUserId()));
                return AjaxResult.success("上传成功", imageUrl);
            } else {
                log.error("更新用户头像信息失败");
                return error("更新数据库失败！");
            }
        } catch (Exception e) {
            log.error("头像上传过程发生异常", e);
            return AjaxResult.error("上传失败：" + e.getMessage());
        }
    }


    /**
     * 个人中心 - 个性化学习路线
     */
    @GetMapping("/learning-path")
    @ResponseBody
    public AjaxResult learningPath() {
        SysUser user = getSysUser();
        if (user == null || user.getUserId() == null) {
            return AjaxResult.error("请先登录");
        }
        Long userId = user.getUserId();
        LearningAnalysis analysis = learningAnalysisService.analyzeUserLearning(userId);
        LearningPath path = learningAnalysisService.generateLearningPath(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("analysis", analysis);
        data.put("path", path);
        return AjaxResult.success(data);
    }

}
