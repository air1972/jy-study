package com.jy.study.web.controller.web;

import com.github.pagehelper.PageHelper;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.service.IStudyLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台分类页面Controller
 */
@Controller
@RequestMapping("/web/category")
public class WebCategoryController extends BaseController {

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private IStudyArticleService articleService;

    /**
     * 课程分类页面
     */
    @GetMapping("/lesson")
    public String lessonCategory(ModelMap mmap) {
        // 获取第一页课程数据
        PageHelper.startPage(1, 12);
        List<StudyLesson> lessons = lessonService.selectStudyLessonList(new StudyLesson());
        TableDataInfo tableData = getDataTable(lessons);
        
        mmap.put("lessons", tableData.getRows());
        // 当前页码固定为1
        mmap.put("pageNum", 1);
        // 计算总页数
        long totalPages = (tableData.getTotal() + 11) / 12; // 12是每页大小，11是(12-1)
        mmap.put("totalPages", totalPages);

        return "/web/lesson-category";
    }

    /**
     * 获取课程列表数据
     */
    @PostMapping("/lessonList")
    @ResponseBody
    public AjaxResult lessonList(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(required = false) String version,
                          @RequestParam(required = false) String subject,
                          @RequestParam(required = false) String grade,
                          @RequestParam(required = false) String volume,
                          @RequestParam(required = false) String keyword) {
        
        PageHelper.startPage(pageNum, 12);

        StudyLesson query = new StudyLesson();
        
        // 四维分类筛选
        if (version != null && !version.trim().isEmpty()) {
            query.setVersionName(version.trim());
        }
        if (subject != null && !subject.trim().isEmpty()) {
            query.setSubjectName(subject.trim());
        }
        if (grade != null && !grade.trim().isEmpty()) {
            query.setGradeName(grade.trim());
        }
        if (volume != null && !volume.trim().isEmpty()) {
            query.setVolumeName(volume.trim());
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setTitle(keyword.trim());
        }

        List<StudyLesson> lessons = lessonService.selectStudyLessonList(query);
        TableDataInfo tableData = getDataTable(lessons);

        Map<String, Object> data = new HashMap<>();
        data.put("lessons", tableData.getRows());
        data.put("pageNum", pageNum);
        // 计算总页数
        long totalPages = (tableData.getTotal() + 11) / 12;
        data.put("totalPages", totalPages);

        return AjaxResult.success(data);
    }

    /**
     * 文章分类页面
     */
    @GetMapping("/article")
    public String articleCategory(ModelMap mmap) {
        // 获取第一页文章数据
        PageHelper.startPage(1, 12);
        List<StudyArticle> articles = articleService.selectArticleList(new StudyArticle());
        TableDataInfo tableData = getDataTable(articles);
        
        mmap.put("articles", tableData.getRows());
        mmap.put("pageNum", 1);
        long totalPages = (tableData.getTotal() + 11) / 12;
        mmap.put("totalPages", totalPages);

        return "web/article-category";
    }

    /**
     * 获取文章列表数据
     */
    @PostMapping("/articleList")
    @ResponseBody
    public AjaxResult articleList(@RequestParam(defaultValue = "1") Integer pageNum,
                      @RequestParam(required = false) String version,
                      @RequestParam(required = false) String subject,
                      @RequestParam(required = false) String grade,
                      @RequestParam(required = false) String volume,
                      @RequestParam(required = false) String keyword) {
        
        PageHelper.startPage(pageNum, 12);

        StudyArticle query = new StudyArticle();
        
        // 四维分类筛选
        if (version != null && !version.trim().isEmpty()) {
            query.setVersionName(version.trim());
        }
        if (subject != null && !subject.trim().isEmpty()) {
            query.setSubjectName(subject.trim());
        }
        if (grade != null && !grade.trim().isEmpty()) {
            query.setGradeName(grade.trim());
        }
        if (volume != null && !volume.trim().isEmpty()) {
            query.setVolumeName(volume.trim());
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setTitle(keyword.trim());
        }

        List<StudyArticle> articles = articleService.selectArticleList(query);
        TableDataInfo tableData = getDataTable(articles);

        Map<String, Object> data = new HashMap<>();
        data.put("articles", tableData.getRows());
        data.put("pageNum", pageNum);
        long totalPages = (tableData.getTotal() + 11) / 12;
        data.put("totalPages", totalPages);

        return AjaxResult.success(data);
    }
}
