package com.jy.study.web.controller.study;

import java.util.List;

import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.service.IStudyLessonExerciseService;
import com.jy.study.lesson.service.IStudyLessonService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jy.study.common.annotation.Log;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.common.core.page.TableDataInfo;

/**
 * 课程Controller
 * 
 * @author jily
 * @date 2025-01-14
 */
@Controller
@RequestMapping("/lesson")
public class LessonController extends BaseController
{
    private String prefix = "lesson";

    @Autowired
    private IStudyLessonService studyLessonService;

    @Autowired
    private IStudyLessonExerciseService studyLessonExerciseService;

    //就是用来标识是否看得见菜单
    @RequiresPermissions("system:lesson:view")
    @GetMapping()
    public String lesson()
    {
        return prefix + "/lesson";
    }

    /**
     * 查询课程列表
     */
    @RequiresPermissions("system:lesson:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyLesson studyLesson)
    {
        startPage();
        List<StudyLesson> list = studyLessonService.selectStudyLessonList(studyLesson);
        return getDataTable(list);
    }

    /**
     * 获取所有课程列表（下拉框使用）
     */
    @GetMapping("/listAll")
    @ResponseBody
    public TableDataInfo listAll(StudyLesson studyLesson)
    {
        // 只查询正常状态的课程
        studyLesson.setStatus("0");
        List<StudyLesson> list = studyLessonService.selectStudyLessonList(studyLesson);
        return getDataTable(list);
    }

    /**
     * 导出课程列表
     */
    @RequiresPermissions("system:lesson:export")
    @Log(title = "课程", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StudyLesson studyLesson)
    {
        List<StudyLesson> list = studyLessonService.selectStudyLessonList(studyLesson);
        ExcelUtil<StudyLesson> util = new ExcelUtil<StudyLesson>(StudyLesson.class);
        return util.exportExcel(list, "课程数据");
    }

    /**
     * 新增课程
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存课程
     */
    @RequiresPermissions("system:lesson:add")
    @Log(title = "课程", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyLesson studyLesson)
    {
        return toAjax(studyLessonService.insertStudyLesson(studyLesson));
    }

    /**
     * 修改课程
     */
    @RequiresPermissions("system:lesson:edit")
    @GetMapping("/edit/{lessonId}")
    public String edit(@PathVariable("lessonId") Long lessonId, ModelMap mmap)
    {
        StudyLesson studyLesson = studyLessonService.selectStudyLessonByLessonId(lessonId);
        mmap.put("studyLesson", studyLesson);
        return prefix + "/edit";
    }

    /**
     * 修改保存课程
     */
    @RequiresPermissions("system:lesson:edit")
    @Log(title = "课程", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyLesson studyLesson)
    {
        return toAjax(studyLessonService.updateStudyLesson(studyLesson));
    }

    /**
     * 删除课程
     */
    @RequiresPermissions("system:lesson:remove")
    @Log(title = "课程", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyLessonService.deleteStudyLessonByLessonIds(ids));
    }

    @GetMapping("/detail/{lessonId}")
    public String detail(@PathVariable("lessonId") Long lessonId, ModelMap mmap) {
        StudyLesson studyLesson = studyLessonService.selectStudyLessonByLessonId(lessonId);
        // 获取该课程的练习列表
        List<StudyLessonExercise> exercises = studyLessonExerciseService.selectStudyLessonExerciseByLessonId(lessonId);
        studyLesson.setExercises(exercises);
        mmap.put("studyLesson", studyLesson);
        return prefix + "/detail";
    }
}
