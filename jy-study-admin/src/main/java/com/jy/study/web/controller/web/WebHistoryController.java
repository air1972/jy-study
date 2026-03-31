package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.lesson.domain.dto.UserViewHistoryDTO;
import com.jy.study.lesson.service.IStudyUserInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户浏览记录控制器
 */
@Controller
@RequestMapping("/web/user/history")
public class WebHistoryController extends BaseController {

    @Autowired
    private IStudyUserInteractionService interactionService;

    /**
     * 浏览记录页面
     */
    @GetMapping()
    public String history(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);

        // 获取用户浏览历史记录（包含详细信息）
        List<UserViewHistoryDTO> viewHistory = interactionService.getUserViewHistoryWithDetails(user.getUserId(), 50);

        // 分离课程和文章浏览记录
        List<UserViewHistoryDTO> courseHistory = viewHistory.stream()
                .filter(v -> "1".equals(v.getType()))
                .collect(Collectors.toList());

        List<UserViewHistoryDTO> articleHistory = viewHistory.stream()
                .filter(v -> "2".equals(v.getType()))
                .collect(Collectors.toList());

        mmap.put("courseHistory", courseHistory);
        mmap.put("articleHistory", articleHistory);

        return "web/user/history";
    }
}