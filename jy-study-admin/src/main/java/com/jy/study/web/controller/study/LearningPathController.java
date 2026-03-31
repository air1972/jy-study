package com.jy.study.web.controller.study;

import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.lesson.service.ILearningAnalysisService;
import com.jy.study.lesson.domain.LearningAnalysis;
import com.jy.study.lesson.domain.LearningPath;
import com.jy.study.common.utils.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 个性化学习路径控制器
 */
@Controller
@RequestMapping("/study/learning-path")
public class LearningPathController extends BaseController {

    @Autowired
    private ILearningAnalysisService learningAnalysisService;

    /**
     * 学习路径页面
     */
    @RequiresPermissions("study:learningPath:view")
    @GetMapping()
    public String learningPath() {
        return "study/learningPath";
    }

    /**
     * 获取学习分析
     */
    @RequiresPermissions("study:learningPath:view")
    @GetMapping("/analysis")
    @ResponseBody
    public AjaxResult getLearningAnalysis() {
        Long userId = ShiroUtils.getUserId();
        LearningAnalysis analysis = learningAnalysisService.analyzeUserLearning(userId);
        return AjaxResult.success(analysis);
    }

    /**
     * 生成学习路径
     */
    @RequiresPermissions("study:learningPath:generate")
    @GetMapping("/generate")
    @ResponseBody
    public AjaxResult generateLearningPath() {
        Long userId = ShiroUtils.getUserId();
        LearningPath path = learningAnalysisService.generateLearningPath(userId);
        return AjaxResult.success(path);
    }

    /**
     * 获取当前学习路径
     */
    @RequiresPermissions("study:learningPath:view")
    @GetMapping("/current")
    @ResponseBody
    public AjaxResult getCurrentLearningPath() {
        Long userId = ShiroUtils.getUserId();
        LearningPath path = learningAnalysisService.generateLearningPath(userId);
        return AjaxResult.success(path);
    }
}
