package com.jy.study.web.controller.web;

import com.jy.study.web.controller.common.CommonController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.kaptcha.Constants;
import com.jy.study.common.constant.UserConstants;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.common.utils.MessageUtils;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.framework.manager.AsyncManager;
import com.jy.study.framework.manager.factory.AsyncFactory;
import com.jy.study.framework.shiro.service.SysPasswordService;
import com.jy.study.framework.web.service.ConfigService;
import com.jy.study.system.service.ISysUserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/web")
public class WebAuthController extends BaseController {

    /**
     * 是否开启记住我功能
     */
    @Value("${shiro.rememberMe.enabled: true}")
    private boolean rememberMe;

    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPasswordService passwordService;
    
    @Autowired
    private ConfigService configService;

    @GetMapping("/checkLogin")
    @ResponseBody
    public AjaxResult checkLogin() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && (subject.isAuthenticated() || subject.isRemembered()) && subject.getPrincipal() != null) {
            return success();
        }
        return error("未登录");
    }
} 
