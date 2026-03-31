package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.exception.ServiceException;
import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyComment;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.service.IStudyCommentService;
import com.jy.study.lesson.service.IStudyLessonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/web/comments")
public class WebCommentController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(WebCommentController.class);

    @Autowired
    private IStudyCommentService commentService;

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private IStudyArticleService articleService;

    @GetMapping("/list")
    public AjaxResult list(String type, Long targetId, Integer pageNum, Integer pageSize) {
        AjaxResult validateResult = validateTarget(type, targetId);
        if (validateResult != null) {
            return validateResult;
        }

        try {
            int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
            int safePageSize = pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 20);
            Long totalCount = commentService.countComments(type, targetId);
            Map<String, Object> data = new HashMap<>();
            data.put("count", totalCount);
            data.put("comments", commentService.selectComments(type, targetId, safePageNum, safePageSize));
            data.put("pageNum", safePageNum);
            data.put("pageSize", safePageSize);
            data.put("hasMore", totalCount > (long) safePageNum * safePageSize);
            return AjaxResult.success(data);
        } catch (Exception e) {
            log.error("Load comments failed, type={}, targetId={}", type, targetId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/add")
    public AjaxResult add(String type, Long targetId, String content) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }

        AjaxResult validateResult = validateTarget(type, targetId);
        if (validateResult != null) {
            return validateResult;
        }

        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isEmpty()) {
            return error("评论内容不能为空");
        }
        if (normalizedContent.length() > 500) {
            return error("评论内容不能超过500个字符");
        }

        try {
            StudyComment comment = commentService.addComment(userId, type, targetId, normalizedContent);
            Map<String, Object> data = new HashMap<>();
            data.put("count", commentService.countComments(type, targetId));
            data.put("comment", comment);
            return AjaxResult.success("评论发布成功", data);
        } catch (ServiceException e) {
            return AjaxResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("Add comment failed, userId={}, type={}, targetId={}", userId, type, targetId, e);
            return AjaxResult.error(buildFriendlyMessage(e));
        }
    }

    @PostMapping("/delete")
    public AjaxResult delete(Long commentId) {
        Long userId = ShiroUtils.getUserId();
        if (userId == null) {
            return error("请先登录");
        }
        if (commentId == null) {
            return AjaxResult.error("参数错误");
        }
        boolean deleted = commentService.deleteOwnComment(commentId, userId);
        return deleted ? AjaxResult.success("评论已删除") : AjaxResult.error("只能删除自己的评论");
    }

    private AjaxResult validateTarget(String type, Long targetId) {
        if (targetId == null) {
            return AjaxResult.error("参数错误");
        }
        if (!"1".equals(type) && !"2".equals(type)) {
            return AjaxResult.error("类型错误");
        }

        if ("1".equals(type)) {
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(targetId);
            if (lesson == null || !"0".equals(lesson.getStatus())) {
                return AjaxResult.error("课程不存在或已下架");
            }
            return null;
        }

        StudyArticle article = articleService.selectArticleById(targetId);
        if (article == null || !"0".equals(article.getStatus())) {
            return AjaxResult.error("文章不存在或已下架");
        }
        return null;
    }

    private String buildFriendlyMessage(Exception e) {
        String message = e == null ? "" : String.valueOf(e.getMessage());
        if (message.contains("study_comment") || message.contains("doesn't exist")) {
            return "评论功能还没有完成数据库初始化，请先执行 sql/add_comment_feature.sql";
        }
        return "评论功能暂时不可用，请稍后再试";
    }
}
