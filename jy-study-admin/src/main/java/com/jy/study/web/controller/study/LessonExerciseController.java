package com.jy.study.web.controller.study;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jy.study.common.ai.TongYiAI;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.service.IStudyLessonExerciseService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.jy.study.common.annotation.Log;
import com.jy.study.common.enums.BusinessType;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.common.utils.StringUtils;

/**
 * 课程练习 Controller
 *
 * @author jily
 * @date 2026-03-26
 */
@Controller
@RequestMapping("/lesson/exercise")
public class LessonExerciseController extends BaseController
{
    private String prefix = "lesson/exercise";

    @Autowired
    private IStudyLessonExerciseService studyLessonExerciseService;

    @Autowired
    private TongYiAI tongYiAI;

    @RequiresPermissions("lesson:exercise:view")
    @GetMapping()
    public String exercise()
    {
        return prefix + "/exercise";
    }

    /**
     * 查询课程练习列表
     */
    @RequiresPermissions("lesson:exercise:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyLessonExercise studyLessonExercise)
    {
        startPage();
        List<StudyLessonExercise> list = studyLessonExerciseService.selectStudyLessonExerciseList(studyLessonExercise);
        return getDataTable(list);
    }

    /**
     * 新增课程练习
     */
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存课程练习
     */
    @RequiresPermissions("lesson:exercise:add")
    @Log(title = "课程练习", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(StudyLessonExercise studyLessonExercise)
    {
        try {
            if (studyLessonExercise.getLessonId() == null) {
                return AjaxResult.error("请选择课程");
            }
            if (StringUtils.isBlank(studyLessonExercise.getTitle())) {
                return AjaxResult.error("请填写练习标题");
            }
            if (StringUtils.isBlank(studyLessonExercise.getExerciseType())) {
                return AjaxResult.error("请选择题目类型");
            }
            if (StringUtils.isBlank(studyLessonExercise.getAnswer())) {
                return AjaxResult.error("请填写正确答案");
            }
            return toAjax(studyLessonExerciseService.insertStudyLessonExercise(studyLessonExercise));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null && e.getCause() != null) {
                msg = e.getCause().getMessage();
            }
            return AjaxResult.error("保存失败：" + (StringUtils.isBlank(msg) ? e.getClass().getSimpleName() : msg));
        }
    }

    /**
     * 修改课程练习
     */
    @RequiresPermissions("lesson:exercise:edit")
    @GetMapping("/edit/{exerciseId}")
    public String edit(@PathVariable("exerciseId") Long exerciseId, ModelMap mmap)
    {
        StudyLessonExercise studyLessonExercise = studyLessonExerciseService.selectStudyLessonExerciseById(exerciseId);
        mmap.put("studyLessonExercise", studyLessonExercise);
        return prefix + "/edit";
    }

    /**
     * 修改保存课程练习
     */
    @RequiresPermissions("lesson:exercise:edit")
    @Log(title = "课程练习", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(StudyLessonExercise studyLessonExercise)
    {
        return toAjax(studyLessonExerciseService.updateStudyLessonExercise(studyLessonExercise));
    }

    /**
     * 删除课程练习
     */
    @RequiresPermissions("lesson:exercise:remove")
    @Log(title = "课程练习", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(studyLessonExerciseService.deleteStudyLessonExerciseByIds(ids));
    }

    /**
     * AI生成练习页面
     */
    @RequiresPermissions("lesson:exercise:add")
    @GetMapping("/aiGenerate")
    public String aiGenerate()
    {
        return prefix + "/aiGenerate";
    }

    /**
     * AI生成练习
     */
    @RequiresPermissions("lesson:exercise:add")
    @Log(title = "课程练习", businessType = BusinessType.INSERT)
    @PostMapping("/aiGenerate")
    @ResponseBody
    public AjaxResult aiGenerateSave(@RequestParam Long lessonId,
                                      @RequestParam(required = false) String topic,
                                      @RequestParam(defaultValue = "3") Integer xuanze,
                                      @RequestParam(defaultValue = "2") Integer duoxuan,
                                      @RequestParam(defaultValue = "2") Integer panduan,
                                      @RequestParam(defaultValue = "1") Integer jianda,
                                      @RequestParam(defaultValue = "2") String difficulty,
                                      @RequestParam(defaultValue = "10") Integer score)
    {
        try {
            // 构建生成内容
            String content = buildGenerationContent(topic);
            
            // 调用AI生成题目
            String aiResponse = tongYiAI.generateQuestions(xuanze, 0, panduan, jianda, content);
            
            // 解析AI返回的内容
            List<StudyLessonExercise> exercises = parseAIResponse(aiResponse, lessonId, difficulty, score);
            
            // 批量保存生成的练习
            int count = 0;
            for (StudyLessonExercise exercise : exercises) {
                studyLessonExerciseService.insertStudyLessonExercise(exercise);
                count++;
            }
            
            return AjaxResult.success(count);
        } catch (Exception e) {
            return AjaxResult.error("AI生成失败：" + e.getMessage());
        }
    }

    /**
     * 构建生成内容
     */
    private String buildGenerationContent(String topic) {
        if (topic != null && !topic.trim().isEmpty()) {
            return topic;
        }
        return "请生成一些通用的教育练习题";
    }

    /**
     * 解析AI返回的内容
     */
    private List<StudyLessonExercise> parseAIResponse(String aiResponse, Long lessonId, 
                                                       String difficulty, Integer score) {
        List<StudyLessonExercise> exercises = new java.util.ArrayList<>();
        
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return exercises;
        }
        
        // 清理内容
        aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
        
        // 按题型分割
        String[] sections = aiResponse.split("(?=[一二三四五六七八九十]+、)");
        int sort = 1;
        
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            
            // 确定题型
            String exerciseType = determineExerciseType(section);
            
            // 分割题目
            String[] questions = section.split("(?=\\d+、)");
            
            for (String question : questions) {
                if (question.trim().isEmpty() || !question.matches("(?s).*\\d+、.*")) continue;
                
                StudyLessonExercise exercise = parseQuestion(question, lessonId, exerciseType, 
                                                              difficulty, score, sort++);
                if (exercise != null) {
                    exercises.add(exercise);
                }
            }
        }
        
        return exercises;
    }

    /**
     * 确定题型
     */
    private String determineExerciseType(String section) {
        if (section.contains("选择")) return "1";
        if (section.contains("填空")) return "2";
        if (section.contains("判断")) return "3";
        if (section.contains("简答")) return "4";
        return "1";
    }

    /**
     * 解析单个题目
     */
    private StudyLessonExercise parseQuestion(String question, Long lessonId, String exerciseType,
                                               String difficulty, Integer score, Integer sort) {
        StudyLessonExercise exercise = new StudyLessonExercise();
        exercise.setLessonId(lessonId);
        exercise.setExerciseType(exerciseType);
        exercise.setDifficulty(difficulty);
        exercise.setScore(score);
        exercise.setSort(sort);
        exercise.setStatus("0");
        
        // 提取答案和解析
        int answerIndex = question.indexOf("[答案]");
        int correctAnswerIndex = question.indexOf("[正确答案]");
        int analysisIndex = question.indexOf("[解析]");
        
        int firstAnswerIndex = -1;
        int answerLabelLength = 0;
        
        if (answerIndex != -1 && (correctAnswerIndex == -1 || answerIndex < correctAnswerIndex)) {
            firstAnswerIndex = answerIndex;
            answerLabelLength = 4;
        } else if (correctAnswerIndex != -1) {
            firstAnswerIndex = correctAnswerIndex;
            answerLabelLength = 6;
        }
        
        String questionText;
        if (firstAnswerIndex != -1) {
            questionText = question.substring(0, firstAnswerIndex).trim();
            
            // 提取答案
            String answer;
            if (analysisIndex != -1 && analysisIndex > firstAnswerIndex) {
                answer = question.substring(firstAnswerIndex + answerLabelLength, analysisIndex).trim();
            } else {
                answer = question.substring(firstAnswerIndex + answerLabelLength).trim();
            }
            exercise.setAnswer(answer);
            
            // 提取解析
            if (analysisIndex != -1) {
                String analysis = question.substring(analysisIndex + 4).trim();
                exercise.setAnalysis(analysis);
            }
        } else {
            questionText = question.trim();
        }
        
        // 提取标题和选项
        if ("1".equals(exerciseType)) {
            // 选择题，提取选项
            String[] lines = questionText.split("\\n");
            StringBuilder title = new StringBuilder();
            JSONObject options = new JSONObject();
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.matches("^[A-D]\\s*[.．、]\\s*.*$")) {
                    String optionKey = line.substring(0, 1);
                    String optionValue = line.substring(1).replaceAll("^[.．、]\\s*", "").trim();
                    options.put(optionKey, optionValue);
                } else if (line.matches("^\\d+、.*$")) {
                    title.append(line.replaceFirst("^\\d+、\\s*", "")).append(" ");
                } else {
                    title.append(line).append(" ");
                }
            }
            
            exercise.setTitle(title.toString().trim());
            if (!options.isEmpty()) {
                exercise.setOptions(options.toJSONString());
            }
        } else {
            // 其他题型
            exercise.setTitle(questionText.replaceFirst("^\\d+、\\s*", "").replaceAll("\\n", " ").trim());
        }
        
        // 验证必要字段
        if (exercise.getTitle() == null || exercise.getTitle().trim().isEmpty()) {
            return null;
        }
        
        return exercise;
    }
}
