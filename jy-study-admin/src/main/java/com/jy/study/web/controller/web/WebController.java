package com.jy.study.web.controller.web;

import com.github.pagehelper.PageHelper;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.service.IStudyLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 前台首页控制器
 */
@Controller
@RequestMapping("/web")
public class WebController extends BaseController {

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyLessonService lessonService;

    /**
     * 前台首页 - 根路径访问
     */
    @GetMapping("")
    public String root(ModelMap mmap) {
        return index(mmap);
    }

    /**
     * 前台首页 - /web/index路径访问
     */
    @GetMapping("/index")
    public String index(ModelMap mmap) {
        // 获取当前用户
        SysUser user = getSysUser();
        mmap.put("user", user);
        
        // 获取最热文章(前12名)
        StudyArticle article = new StudyArticle();
        article.setStatus("0"); // 只查询正常状态的文章
        PageHelper.startPage(1, 12, "view_count desc"); // 设置分页和排序
        List<StudyArticle> articles = articleService.selectArticleList(article);
        mmap.put("articles", articles);
        
        // 获取最热课程(前6名)
        StudyLesson lesson = new StudyLesson();
        lesson.setStatus("0"); // 只查询正常状态的课程
        PageHelper.startPage(1, 6, "view_count desc"); // 设置分页和排序
        List<StudyLesson> lessons = lessonService.selectStudyLessonList(lesson);
        mmap.put("lessons", lessons);
        
        return "web/index";
    }
}
