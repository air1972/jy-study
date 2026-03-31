package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.lesson.domain.dto.UserLikeDTO;
import com.jy.study.lesson.service.IStudyUserInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/user/like")
public class WebLikeController extends BaseController {

    @Autowired
    private IStudyUserInteractionService interactionService;

    /**
     * 点赞记录页面
     */
    @GetMapping()
    public String like(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);

        // 获取用户点赞记录（包含详细信息）
        List<UserLikeDTO> likes = interactionService.getUserLikeWithDetails(user.getUserId(), 50);

        // 分离课程和文章点赞记录
        List<UserLikeDTO> courseLikes = likes.stream()
                .filter(v -> "1".equals(v.getType()))
                .collect(Collectors.toList());

        List<UserLikeDTO> articleLikes = likes.stream()
                .filter(v -> "2".equals(v.getType()))
                .collect(Collectors.toList());

        mmap.put("courseLikes", courseLikes);
        mmap.put("articleLikes", articleLikes);

        return "web/user/like";
    }
} 