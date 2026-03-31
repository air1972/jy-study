package com.jy.study.framework.web.service;

import org.springframework.stereotype.Service;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.utils.ShiroUtils;

@Service("shiroService")
public class ShiroService {
    /**
     * 获取当前用户
     */
    public SysUser getPrincipal() {
        return ShiroUtils.getSysUser();
    }
} 