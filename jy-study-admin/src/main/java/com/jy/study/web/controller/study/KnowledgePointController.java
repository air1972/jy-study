package com.jy.study.web.controller.study;

import java.util.List;

import com.jy.study.common.ai.TongYiAI;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyArticleService;
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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jy.study.common.annotation.Log;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.lesson.service.IStudyKnowledgePointService;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.common.core.page.TableDataInfo;

/**
 * 知识点Controller
 */
@Controller
@RequestMapping("/study/knowledge")
public class KnowledgePointController extends BaseController {
    private String prefix = "study/knowledge";

    @Autowired
    private IStudyKnowledgePointService studyKnowledgePointService;

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private TongYiAI tongYiAI;

    @RequiresPermissions("study:knowledge:view")
    @GetMapping()
    public String knowledge()
    {
        return prefix + "/knowledge";
    }

    /**
     * AI生成知识点
     */
    @RequiresPermissions("study:knowledge:add")
    @Log(title = "知识点", businessType = BusinessType.INSERT)
    @PostMapping("/ai-generate")
    @ResponseBody
    public AjaxResult aiGenerate(String content)
    {
        try {
            String knowledgePointsJson = tongYiAI.generateKnowledgePoints(content);
            // 去除可能存在的 markdown 代码块标记
            knowledgePointsJson = knowledgePointsJson.replace("```json", "").replace("```", "").trim();
            JSONArray knowledgePoints = JSON.parseArray(knowledgePointsJson);
            for (int i = 0; i < knowledgePoints.size(); i++) {
                JSONObject kp = knowledgePoints.getJSONObject(i);
                StudyKnowledgePoint studyKnowledgePoint = new StudyKnowledgePoint();
                studyKnowledgePoint.setName(kp.getString("name"));
                studyKnowledgePoint.setDescription(kp.getString("description"));
                studyKnowledgePointService.insertStudyKnowledgePoint(studyKnowledgePoint);
            }
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 根据内容ID AI生成知识点
     */
    @RequiresPermissions("study:knowledge:add")
    @Log(title = "知识点", businessType = BusinessType.INSERT)
    @PostMapping("/ai-generate-by-id")
    @ResponseBody
    public AjaxResult aiGenerateById(String contentType, Long contentId)
    {
        try {
            String content = "";
            String sourceTitle = "";
            if ("article".equals(contentType)) {
                StudyArticle article = articleService.selectArticleById(contentId);
                if (article != null) {
                    content = article.getContent();
                    sourceTitle = article.getTitle();
                }
            } else if ("lesson".equals(contentType)) {
                StudyLesson lesson = lessonService.selectStudyLessonByLessonId(contentId);
                if (lesson != null) {
                    content = lesson.getVideoSubtitleText() != null ? lesson.getVideoSubtitleText() : lesson.getDescription();
                    sourceTitle = lesson.getTitle();
                }
            }

            if (content == null || content.isEmpty()) {
                return AjaxResult.error("未找到相关内容或内容为空");
            }

            String knowledgePointsJson = tongYiAI.generateKnowledgePoints(content);
            // 去除可能存在的 markdown 代码块标记
            knowledgePointsJson = knowledgePointsJson.replace("```json", "").replace("```", "").trim();
            JSONArray knowledgePoints = JSON.parseArray(knowledgePointsJson);
            for (int i = 0; i < knowledgePoints.size(); i++) {
                JSONObject kp = knowledgePoints.getJSONObject(i);
                StudyKnowledgePoint studyKnowledgePoint = new StudyKnowledgePoint();
                studyKnowledgePoint.setName(kp.getString("name"));
                studyKnowledgePoint.setDescription(kp.getString("description"));
                studyKnowledgePoint.setContentType(contentType);
                studyKnowledgePoint.setContentId(contentId);
                studyKnowledgePoint.setSourceTitle(sourceTitle);
                studyKnowledgePointService.insertStudyKnowledgePoint(studyKnowledgePoint);
            }
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 查询知识点列表
     */
    @RequiresPermissions("study:knowledge:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyKnowledgePoint studyKnowledgePoint)
    {
        startPage();
        List<StudyKnowledgePoint> list = studyKnowledgePointService.selectStudyKnowledgePointList(studyKnowledgePoint);
        return getDataTable(list);
    }

    /**
     * 获取文章列表供 AI 选择
     */
    @GetMapping("/articles")
    @ResponseBody
    public AjaxResult getArticles() {
        return AjaxResult.success(articleService.selectArticleList(new StudyArticle()));
    }

    /**
     * 获取课程列表供 AI 选择
     */
    @GetMapping("/lessons")
    @ResponseBody
    public AjaxResult getLessons() {
        return AjaxResult.success(lessonService.selectStudyLessonList(new StudyLesson()));
    }

    /**
     * 新增知识点
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存知识点
     */
    @RequiresPermissions("study:knowledge:add")
    @Log(title = "知识点", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyKnowledgePoint studyKnowledgePoint)
    {
        return toAjax(studyKnowledgePointService.insertStudyKnowledgePoint(studyKnowledgePoint));
    }

    /**
     * 修改知识点
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        StudyKnowledgePoint studyKnowledgePoint = new StudyKnowledgePoint();
        studyKnowledgePoint.setId(id);
        mmap.put("studyKnowledgePoint", studyKnowledgePointService.selectStudyKnowledgePointList(studyKnowledgePoint).get(0));
        return prefix + "/edit";
    }

    /**
     * 修改保存知识点
     */
    @RequiresPermissions("study:knowledge:edit")
    @Log(title = "知识点", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyKnowledgePoint studyKnowledgePoint)
    {
        return toAjax(studyKnowledgePointService.updateStudyKnowledgePoint(studyKnowledgePoint));
    }

    /**
     * 删除知识点
     */
    @RequiresPermissions("study:knowledge:remove")
    @Log(title = "知识点", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyKnowledgePointService.deleteStudyKnowledgePointByIds(ids));
    }
}
