package com.jy.study.web.controller.study;

import com.jy.study.common.annotation.Log;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.lesson.service.ArticleQuestionService;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.lesson.domain.StudyAiCoze;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.service.IStudyAiCozeService;
import com.jy.study.lesson.service.IStudyArticleService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章管理 Controller
 *
 * @author jily
 * @date 2024-03-14
 */
@Controller
@RequestMapping("/article")
public class ArtController extends BaseController {

    private String prefix = "article";

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyAiCozeService aiCozeService;

    @RequiresPermissions("article:article:view")
    @GetMapping("/list")
    public String list() {
        return prefix + "/art-list";
    }

    @RequiresPermissions("article:article:list")
    @PostMapping("/listData")
    @ResponseBody
    public TableDataInfo listData(StudyArticle article) {
        startPage();
        List<StudyArticle> list = articleService.selectArticleList(article);
        return getDataTable(list);
    }

    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @Log(title = "文章管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated StudyArticle article) {
        article.setCreateBy(getLoginName());
        return toAjax(articleService.insertArticle(article));
    }

    @GetMapping("/edit/{articleId}")
    public String edit(@PathVariable("articleId") Long articleId, ModelMap mmap) {
        mmap.put("article", articleService.selectArticleById(articleId));
        return prefix + "/edit";
    }

    @Log(title = "文章管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated StudyArticle article) {
        article.setUpdateBy(getLoginName());
        return toAjax(articleService.updateArticle(article));
    }

    @Log(title = "文章管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(articleService.deleteArticleByIds(ids));
    }

    @GetMapping("/detail/{articleId}")
    public String detail(@PathVariable("articleId") Long articleId, ModelMap mmap) {
        mmap.put("article", articleService.selectArticleById(articleId));
        return prefix + "/detail";
    }


    @Autowired
    private ArticleQuestionService articleQuestionService;

    @GetMapping("/ai-questions/{articleId}")
    public String aiQuestions(@PathVariable("articleId") Long articleId, ModelMap mmap) {
        StudyArticle article = articleService.selectArticleById(articleId);
        if (article != null && article.getCozeId() != null) {
            StudyAiCoze aiCoze = aiCozeService.selectAiCozeById(article.getCozeId());
            if (aiCoze != null) {
                // 格式化试题内容
                String formattedContent = articleQuestionService.formatQuestionHtml(aiCoze.getContent());
                mmap.put("formattedContent", formattedContent);
                mmap.put("aiCoze", aiCoze);
                return prefix + "/ai-questions";
            }
        }
        // 如果cozeId为null或aiCoze不存在，重定向回文章列表
        return "redirect:/article/list";
    }
}
