package com.jy.study.web.controller.study;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jy.study.common.ai.TongYiAI;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.lesson.service.IStudyKnowledgePointService;
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
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.service.IStudyExerciseService;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.utils.poi.ExcelUtil;
import com.jy.study.common.core.page.TableDataInfo;

/**
 * 题目Controller
 */
@Controller
@RequestMapping("/study/exercise")
public class ExerciseController extends BaseController {
    private String prefix = "study/exercise";

    @Autowired
    private IStudyExerciseService studyExerciseService;

    @Autowired
    private IStudyKnowledgePointService studyKnowledgePointService;

    @Autowired
    private TongYiAI tongYiAI;

    @RequiresPermissions("study:exercise:view")
    @GetMapping()
    public String exercise()
    {
        return prefix + "/exercise";
    }

    /**
     * 查询题目列表
     */
    @RequiresPermissions("study:exercise:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyExercise studyExercise)
    {
        startPage();
        List<StudyExercise> list = studyExerciseService.selectStudyExerciseList(studyExercise);
        return getDataTable(list);
    }

    /**
     * 新增题目
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存题目
     */
    @RequiresPermissions("study:exercise:add")
    @Log(title = "题目", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyExercise studyExercise)
    {
        return toAjax(studyExerciseService.insertStudyExercise(studyExercise));
    }

    /**
     * 修改题目
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        StudyExercise studyExercise = new StudyExercise();
        studyExercise.setId(id);
        mmap.put("studyExercise", studyExerciseService.selectStudyExerciseList(studyExercise).get(0));
        return prefix + "/edit";
    }

    /**
     * 修改保存题目
     */
    @RequiresPermissions("study:exercise:edit")
    @Log(title = "题目", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyExercise studyExercise)
    {
        return toAjax(studyExerciseService.updateStudyExercise(studyExercise));
    }

    /**
     * 删除题目
     */
    @RequiresPermissions("study:exercise:remove")
    @Log(title = "题目", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyExerciseService.deleteStudyExerciseByIds(ids));
    }

    /**
     * 获取所有知识点列表供 AI 生成题目选择
     */
    @GetMapping("/knowledge-points")
    @ResponseBody
    public AjaxResult getKnowledgePoints() {
        return AjaxResult.success(studyKnowledgePointService.selectStudyKnowledgePointList(new StudyKnowledgePoint()));
    }

    /**
     * AI 生成题目
     */
    @RequiresPermissions("study:exercise:add")
    @Log(title = "题目", businessType = BusinessType.INSERT)
    @PostMapping("/ai-generate")
    @ResponseBody
    public AjaxResult aiGenerate(Long knowledgePointId) {
        try {
            StudyKnowledgePoint kp = new StudyKnowledgePoint();
            kp.setId(knowledgePointId);
            List<StudyKnowledgePoint> list = studyKnowledgePointService.selectStudyKnowledgePointList(kp);
            if (list == null || list.isEmpty()) {
                return AjaxResult.error("未找到相关知识点");
            }
            StudyKnowledgePoint knowledgePoint = list.get(0);
            
            String exerciseJson = tongYiAI.generateExerciseByKnowledge(knowledgePoint.getName(), knowledgePoint.getDescription());
            // 清理可能存在的 markdown 标记
            exerciseJson = exerciseJson.replace("```json", "").replace("```", "").trim();
            
            JSONArray exercises = JSON.parseArray(exerciseJson);
            for (int i = 0; i < exercises.size(); i++) {
                JSONObject obj = exercises.getJSONObject(i);
                StudyExercise exercise = new StudyExercise();
                exercise.setKnowledgePointId(knowledgePointId);
                exercise.setType(obj.getString("type"));
                exercise.setContent(obj.getString("content"));
                exercise.setOptions(obj.getString("options"));
                exercise.setAnswer(obj.getString("answer"));
                exercise.setExplanation(obj.getString("explanation"));
                studyExerciseService.insertStudyExercise(exercise);
            }
            return AjaxResult.success("成功生成 " + exercises.size() + " 道题目");
        } catch (Exception e) {
            return AjaxResult.error("AI 生成失败：" + e.getMessage());
        }
    }
}
