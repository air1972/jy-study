package com.jy.study.web.controller.web;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.lesson.domain.StudyAiCoze;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.ArrayList;

/**
 * 前台页面功能控制器
 */
@Controller
@RequestMapping("/web")
public class WebPagesController extends BaseController {
    
    @Autowired
    private IStudyArticleService articleService;
    @Autowired
    private IStudyLessonService lessonService;
    @Autowired
    private IStudyAiCozeService aiCozeService;
    @Autowired
    private IStudyLessonExerciseService lessonExerciseService;

    /**
     * 文章详情页
     */
    @GetMapping("/article/{articleId}")
    public String article(@PathVariable("articleId") Long articleId, ModelMap mmap) {
        StudyArticle article = articleService.selectArticleById(articleId);
        if (article == null) {
            return "error/404";
        }
        // 只允许查看正常状态的文章
        if (!"0".equals(article.getStatus())) {
            return "error/404";
        }
        // 获取试题预览
        if (article.getCozeId() != null) {
            StudyAiCoze aiCoze = aiCozeService.selectAiCozeById(article.getCozeId());
            if (aiCoze != null) {
                mmap.put("aiCoze", aiCoze);
                // 获取内容预览（前100个字符）
                String previewContent = getPreviewContent(aiCoze.getContent(), 100);
                mmap.put("previewContent", previewContent);
            }
        }

        mmap.put("article", article);
        return "web/article";
    }

    /**
     * 获取预览内容
     */
    private String getPreviewContent(String content, int length) {
        if (content == null) {
            return "";
        }
        // 去除 HTML 标签和换行符
        String text = content.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ");
        if (text.length() <= length) {
            return text;
        }
        return text.substring(0, length) + "...";
    }

    /**
     * 搜索页面
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         HttpServletRequest request,
                         ModelMap mmap) {
        keyword = normalizeKeyword(keyword, request);

        StudyArticle article = new StudyArticle();
        article.setStatus("0");
        if (keyword != null && !keyword.trim().isEmpty()) {
            article.setTitle(keyword);
        }
        List<StudyArticle> articles = articleService.selectArticleList(article);

        StudyLesson lesson = new StudyLesson();
        lesson.setStatus("0");
        if (keyword != null && !keyword.trim().isEmpty()) {
            lesson.setTitle(keyword);
        }
        List<StudyLesson> lessons = lessonService.selectStudyLessonList(lesson);

        mmap.put("lessons", lessons);
        mmap.put("articles", articles);
        mmap.put("keyword", keyword);
        return "web/search";
    }

    private String normalizeKeyword(String keyword, HttpServletRequest request) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return keyword.trim();
        }
        String query = request == null ? null : request.getQueryString();
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        if (query.startsWith("keyword-")) {
            return query.substring("keyword-".length()).trim();
        }
        int idx = query.indexOf("keyword-");
        if (idx >= 0) {
            return query.substring(idx + "keyword-".length()).trim();
        }
        return null;
    }


    /**
     * 精选推荐页
     */
    @GetMapping("/featured")
    public String featured(ModelMap mmap) {
        StudyArticle article = new StudyArticle();
        article.setStatus("0");
        List<StudyArticle> articles = articleService.selectArticleList(article);
        mmap.put("articles", articles);
        return "web/featured";
    }

    /**
     * 课程详情页
     */
    @GetMapping("/lesson/{lessonId}")
    public String lesson(@PathVariable("lessonId") Long lessonId, ModelMap mmap) {
        try {
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if (lesson == null || !"0".equals(lesson.getStatus())) {
                return "error/404";
            }
            mmap.put("lesson", lesson);
            mmap.put("subtitlePlainText", stripSubtitleTimelineTag(lesson.getVideoSubtitleText()));

            // 获取该课程的练习列表
            List<StudyLessonExercise> exercises = lessonExerciseService.selectStudyLessonExerciseByLessonId(lessonId);
            if (exercises == null) {
                exercises = new ArrayList<>();
            }
            mmap.put("exercises", exercises);

            return "web/lesson";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    private String stripSubtitleTimelineTag(String subtitleText) {
        if (subtitleText == null) {
            return "";
        }
        String[] lines = subtitleText.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String text = line.replaceFirst("^\\[\\[\\d+,\\d+\\]\\]", "").trim();
            if (!text.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(text);
            }
        }
        return sb.toString();
    }


    /**
     * 课程列表页
     */
    @GetMapping("/lessons")
    public String lessons(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);

        // 获取课程列表
        StudyLesson lesson = new StudyLesson();
        lesson.setStatus("0"); // 只查询正常状态的课程
        List<StudyLesson> lessons = lessonService.selectStudyLessonList(lesson);
        mmap.put("lessons", lessons);

        return "web/lessons";
    }

    /**
     * 文章列表页
     */
    @GetMapping("/articles")
    public String articles(ModelMap mmap) {
        SysUser user = getSysUser();
        mmap.put("user", user);

        // 获取文章列表
        StudyArticle article = new StudyArticle();
        article.setStatus("0"); // 只查询正常状态的文章
        List<StudyArticle> articles = articleService.selectArticleList(article);
        mmap.put("articles", articles);

        return "web/articles";
    }

    @Autowired
    private ArticleQuestionService articleQuestionService;

    /**
     * 试题详情页
     */
    @GetMapping("/questions/{articleId}")
    public String questions(@PathVariable("articleId") Long articleId, ModelMap mmap) {
        // 1. 获取文章信息
        StudyArticle article = articleService.selectArticleById(articleId);
        if (article == null || !"0".equals(article.getStatus())) {
            return "error/404";
        }
        mmap.put("article", article);
        
        // 2. 获取试题信息
        if (article.getCozeId() == null) {
            return "error/404";
        }
        
        StudyAiCoze aiCoze = aiCozeService.selectAiCozeById(article.getCozeId());
        if (aiCoze == null) {
            return "error/404";
        }
        mmap.put("aiCoze", aiCoze);
        
        String formattedContent = articleQuestionService.formatQuestionHtmlForSubmit(articleId, article.getTitle(), aiCoze.getId(), aiCoze.getContent());
        mmap.put("formattedContent", formattedContent);
        
        return "web/question";
    }

} 
