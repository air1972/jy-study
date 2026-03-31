package com.jy.study.web.controller.study;

import java.util.List;
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
import com.jy.study.lesson.domain.StudyLessonCategory;
import com.jy.study.lesson.service.IStudyLessonCategoryService;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.common.core.page.TableDataInfo;

/**
 * 课程分类Controller
 * 
 * @author jily
 * @date 2024-03-14
 */
@Controller
@RequestMapping("/lesson/category")
public class LessonCategoryController extends BaseController
{
    private String prefix = "lesson/category";

    @Autowired
    private IStudyLessonCategoryService studyLessonCategoryService;

    @RequiresPermissions("lesson:category:view")
    @GetMapping()
    public String category()
    {
        return prefix + "/category";
    }

    /**
     * 查询课程分类列表
     */
    @RequiresPermissions("lesson:category:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyLessonCategory studyLessonCategory)
    {
        startPage();
        List<StudyLessonCategory> list = studyLessonCategoryService.selectStudyLessonCategoryList(studyLessonCategory);
        return getDataTable(list);
    }

    /**
     * 导出课程分类列表
     */
    @RequiresPermissions("lesson:category:export")
    @Log(title = "课程分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StudyLessonCategory studyLessonCategory)
    {
        List<StudyLessonCategory> list = studyLessonCategoryService.selectStudyLessonCategoryList(studyLessonCategory);
        ExcelUtil<StudyLessonCategory> util = new ExcelUtil<StudyLessonCategory>(StudyLessonCategory.class);
        return util.exportExcel(list, "课程分类数据");
    }

    /**
     * 新增课程分类
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存课程分类
     */
    @RequiresPermissions("lesson:category:add")
    @Log(title = "课程分类", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyLessonCategory studyLessonCategory)
    {
        return toAjax(studyLessonCategoryService.insertStudyLessonCategory(studyLessonCategory));
    }

    /**
     * 修改课程分类
     */
    @RequiresPermissions("lesson:category:edit")
    @GetMapping("/edit/{categoryId}")
    public String edit(@PathVariable("categoryId") Long categoryId, ModelMap mmap)
    {
        StudyLessonCategory studyLessonCategory = studyLessonCategoryService.selectStudyLessonCategoryByCategoryId(categoryId);
        mmap.put("studyLessonCategory", studyLessonCategory);
        return prefix + "/edit";
    }

    /**
     * 修改保存课程分类
     */
    @RequiresPermissions("lesson:category:edit")
    @Log(title = "课程分类", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyLessonCategory studyLessonCategory)
    {
        return toAjax(studyLessonCategoryService.updateStudyLessonCategory(studyLessonCategory));
    }

    /**
     * 删除课程分类
     */
    @RequiresPermissions("lesson:category:remove")
    @Log(title = "课程分类", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyLessonCategoryService.deleteStudyLessonCategoryByCategoryIds(ids));
    }

    /**
     * 查看课程分类详情
     */
    @GetMapping("/detail/{categoryId}")
    public String detail(@PathVariable("categoryId") Long categoryId, ModelMap mmap)
    {
        mmap.put("studyLessonCategory", studyLessonCategoryService.selectStudyLessonCategoryByCategoryId(categoryId));
        return prefix + "/detail";
    }
} 