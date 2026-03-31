package com.jy.study.web.controller.stat;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.lesson.service.*;
import com.jy.study.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/system/stat")
public class SysStatisticsController extends BaseController
{

    @Autowired
    private IStudyLessonCategoryService lessonCategoryService;

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyArticleCategoryService articleCategoryService;

    @Autowired
    private ISysUserService userService;

    //用户操作行为、点赞、浏览、收藏
    @Autowired
    private IStudyUserInteractionService userInteractionService;

    /**
     * 统计页面
     */
    @GetMapping()
    public String stat() {
        return "main_v1";
    }

    /**
     * 获取顶部统计数据
     */
    @GetMapping("/topData")
    @ResponseBody
    public AjaxResult getTopData() {
        Map<String, Object> data = new HashMap<>();
        // 用户统计
        data.put("totalUsers", userService.selectUserCount(null));
        data.put("todayActiveUsers", userService.selectUserCount("today"));
        data.put("monthNewUsers", userService.selectUserCount("month"));
        
        // 内容统计
        data.put("totalLessons", lessonService.selectLessonCount(null));
        data.put("totalArticles", articleService.selectArticleCount(null));
        data.put("monthNewLessons", lessonService.selectLessonCount("month"));
        data.put("monthNewArticles", articleService.selectArticleCount("month"));
        
        return AjaxResult.success(data);
    }

    /**
     * 获取内容增长趋势
     */
    @GetMapping("/contentTrend")
    @ResponseBody
    public AjaxResult getContentTrend() {
        Map<String, Object> data = new HashMap<>();
        data.put("lessonTrend", lessonService.selectLessonTrend());
        data.put("articleTrend", articleService.selectArticleTrend());
        return AjaxResult.success(data);
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/categoryStats")
    @ResponseBody
    public AjaxResult getCategoryStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("lessonCategories", lessonService.selectCategoryStats());
        data.put("articleCategories", articleService.selectCategoryStats());
        return AjaxResult.success(data);
    }

    /**
     * 获取排行榜数据
     */
    @GetMapping("/topList")
    @ResponseBody
    public AjaxResult getTopList() {
        Map<String, Object> data = new HashMap<>();
        data.put("topLessons", lessonService.selectTopLessons(10));
        data.put("topArticles", articleService.selectTopArticles(10));
        return AjaxResult.success(data);
    }

    /**
     * 获取今日学习用户信息
     */
    @GetMapping("/activeUsersList")
    @ResponseBody
    public AjaxResult getactiveUsersList() {
        return AjaxResult.success(userInteractionService.selectRecentActiveUsers());
    }
}
