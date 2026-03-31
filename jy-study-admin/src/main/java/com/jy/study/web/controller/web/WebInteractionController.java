package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.lesson.service.IStudyLessonService;
import com.jy.study.lesson.service.IStudyUserInteractionService;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.web.controller.common.CommonController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.apache.shiro.SecurityUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.subject.Subject;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/web/interaction")
public class WebInteractionController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(WebInteractionController.class);
    @Autowired
    private IStudyUserInteractionService interactionService;
    
    @Autowired
    private IStudyArticleService articleService;
    
    @Autowired
    private IStudyLessonService lessonService;


    @PostMapping("/like")
    public AjaxResult like(String type, Long targetId, HttpServletRequest request) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            log.warn("Like operation failed - User not authenticated");
            return error("请先登录");
        }
        boolean result = interactionService.like(userId, type, targetId);
        return result ? success() : error("您已经点赞过了");
    }
    
    @PostMapping("/unlike")
    public AjaxResult unlike(String type, Long targetId, HttpServletRequest request) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        boolean result = interactionService.unlike(userId, type, targetId);
        return result ? success() : error("您还没有点赞");
    }
    
    @PostMapping("/collect")
    public AjaxResult collect(String type, Long targetId, HttpServletRequest request){
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        boolean result = interactionService.collect(userId, type, targetId);
        return result ? success() : error("您已经收藏过了");
    }
    
    @PostMapping("/uncollect")
    public AjaxResult uncollect(String type, Long targetId, HttpServletRequest request) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        boolean result = interactionService.uncollect(userId, type, targetId);
        return result ? success() : error("您还没有收藏");
    }
    
    @GetMapping("/status")
    public AjaxResult getStatus(String type, Long targetId, HttpServletRequest request) {
        Long userId = ShiroUtils.getUserId();
        if(userId == null){
            return success()
                    .put("data", new StatusResult(false, false));
        }
        boolean liked = false;
        boolean collected = false;
        if (userId != null) {
            liked = interactionService.checkLiked(userId, type, targetId);
            collected = interactionService.checkCollected(userId, type, targetId);
        }
        
        return success()
            .put("data", new StatusResult(liked, collected));
    }
    
    @PostMapping("/view")
    @ResponseBody
    public AjaxResult recordView(String type, Long targetId, HttpServletRequest request) {
        if (StringUtils.isEmpty(type) || targetId == null) {
            return AjaxResult.error("参数错误");
        }
        
        // 验证目标是否存在且状态正常
        if ("1".equals(type)) {
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(targetId);
            if (lesson == null || !"0".equals(lesson.getStatus())) {
                return AjaxResult.error("课程不存在或已下架");
            }
        } else if ("2".equals(type)) {
            StudyArticle article = articleService.selectArticleById(targetId);
            if (article == null || !"0".equals(article.getStatus())) {
                return AjaxResult.error("文章不存在或已下架");
            }
        } else {
            return AjaxResult.error("类型错误");
        }

        // 记录浏览信息
        interactionService.recordView(type, targetId);
        return AjaxResult.success();
    }
    
    // 内部类用于返回状态
    private static class StatusResult {
        private boolean liked;
        private boolean collected;
        
        public StatusResult(boolean liked, boolean collected) {
            this.liked = liked;
            this.collected = collected;
        }
        
        public boolean isLiked() {
            return liked;
        }
        
        public boolean isCollected() {
            return collected;
        }
    }
} 
