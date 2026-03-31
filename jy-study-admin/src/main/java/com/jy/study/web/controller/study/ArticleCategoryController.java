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
import com.jy.study.lesson.domain.StudyArticleCategory;
import com.jy.study.lesson.service.IStudyArticleCategoryService;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.common.core.page.TableDataInfo;

/**
 * 文章分类Controller
 * 
 * @author jily
 * @date 2024-03-14
 */
@Controller
@RequestMapping("/article/category")
public class ArticleCategoryController extends BaseController
{
    private String prefix = "article/category";

    @Autowired
    private IStudyArticleCategoryService studyArticleCategoryService;

    @RequiresPermissions("article:category:view")
    @GetMapping()
    public String category()
    {
        return prefix + "/category";
    }

    /**
     * 查询文章分类列表
     */
    @RequiresPermissions("article:category:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyArticleCategory studyArticleCategory)
    {
        startPage();
        List<StudyArticleCategory> list = studyArticleCategoryService.selectStudyArticleCategoryList(studyArticleCategory);
        return getDataTable(list);
    }

    /**
     * 导出文章分类列表
     */
    @RequiresPermissions("article:category:export")
    @Log(title = "文章分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StudyArticleCategory studyArticleCategory)
    {
        List<StudyArticleCategory> list = studyArticleCategoryService.selectStudyArticleCategoryList(studyArticleCategory);
        ExcelUtil<StudyArticleCategory> util = new ExcelUtil<StudyArticleCategory>(StudyArticleCategory.class);
        return util.exportExcel(list, "文章分类数据");
    }

    /**
     * 新增文章分类
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存文章分类
     */
    @RequiresPermissions("article:category:add")
    @Log(title = "文章分类", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyArticleCategory studyArticleCategory)
    {
        return toAjax(studyArticleCategoryService.insertStudyArticleCategory(studyArticleCategory));
    }

    /**
     * 修改文章分类
     */
    @RequiresPermissions("article:category:edit")
    @GetMapping("/edit/{categoryId}")
    public String edit(@PathVariable("categoryId") Long categoryId, ModelMap mmap)
    {
        StudyArticleCategory studyArticleCategory = studyArticleCategoryService.selectStudyArticleCategoryByCategoryId(categoryId);
        mmap.put("studyArticleCategory", studyArticleCategory);
        return prefix + "/edit";
    }

    /**
     * 修改保存文章分类
     */
    @RequiresPermissions("article:category:edit")
    @Log(title = "文章分类", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyArticleCategory studyArticleCategory)
    {
        return toAjax(studyArticleCategoryService.updateStudyArticleCategory(studyArticleCategory));
    }

    /**
     * 删除文章分类
     */
    @RequiresPermissions("article:category:remove")
    @Log(title = "文章分类", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyArticleCategoryService.deleteStudyArticleCategoryByCategoryIds(ids));
    }

    /**
     * 查看文章分类详情
     */
    @GetMapping("/detail/{categoryId}")
    public String detail(@PathVariable("categoryId") Long categoryId, ModelMap mmap)
    {
        mmap.put("studyArticleCategory", studyArticleCategoryService.selectStudyArticleCategoryByCategoryId(categoryId));
        return prefix + "/detail";
    }
} 