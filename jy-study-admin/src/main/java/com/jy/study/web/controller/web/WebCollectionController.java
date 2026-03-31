package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.lesson.domain.dto.UserCollectionDTO;
import com.jy.study.lesson.service.IStudyUserInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/user/collection")
public class WebCollectionController extends BaseController {

    @Autowired
    private IStudyUserInteractionService interactionService;

    /**
     * 收藏页面
     */
    @GetMapping()
    public String collection(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);

        // 获取用户收藏记录（包含详细信息）
        List<UserCollectionDTO> collections = interactionService.getUserCollectionWithDetails(user.getUserId(), 50);

        // 分离课程和文章收藏记录
        List<UserCollectionDTO> courseCollections = collections.stream()
                .filter(v -> "1".equals(v.getType()))
                .collect(Collectors.toList());

        List<UserCollectionDTO> articleCollections = collections.stream()
                .filter(v -> "2".equals(v.getType()))
                .collect(Collectors.toList());

        mmap.put("courseCollections", courseCollections);
        mmap.put("articleCollections", articleCollections);

        return "web/user/collection";
    }
} 