package com.jy.study.web.controller.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.jy.study.common.ai.TongYiAI;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.core.domain.entity.SysUser;
import com.jy.study.common.core.page.TableDataInfo;
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.domain.StudyExerciseRecord;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.lesson.domain.StudyWrongAnswer;
import com.jy.study.lesson.service.IStudyExerciseRecordService;
import com.jy.study.lesson.service.IStudyExerciseService;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.service.IStudyLessonService;
import com.jy.study.lesson.service.IStudyLessonExerciseService;
import com.jy.study.lesson.service.IStudyKnowledgePointService;
import com.jy.study.lesson.service.IStudyWrongAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台练习Controller
 */
@Controller
@RequestMapping("/web/exercise")
public class WebExerciseController extends BaseController {

    @Autowired
    private IStudyKnowledgePointService knowledgePointService;
    @Autowired
    private IStudyArticleService articleService;
    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private IStudyExerciseService exerciseService;

    @Autowired
    private IStudyWrongAnswerService wrongAnswerService;
    @Autowired
    private IStudyLessonExerciseService lessonExerciseService;

    @Autowired
    private TongYiAI tongYiAI;

    /**
     * 习题库首页
     */
    @GetMapping()
    public String exercise() {
        return "web/exercise";
    }

    /**
     * 获取知识点列表（分页）
     */
    @GetMapping("/knowledge/list")
    @ResponseBody
    public TableDataInfo knowledgeList(StudyKnowledgePoint knowledgePoint) {
        startPage();
        List<StudyKnowledgePoint> list = knowledgePointService.selectStudyKnowledgePointList(knowledgePoint);
        return getDataTable(list);
    }

    /**
     * 前台筛选用：文章来源列表
     */
    @GetMapping("/knowledge/articles")
    @ResponseBody
    public AjaxResult knowledgeArticles() {
        StudyArticle query = new StudyArticle();
        query.setStatus("0");
        return AjaxResult.success(articleService.selectArticleList(query));
    }

    /**
     * 前台筛选用：课程来源列表
     */
    @GetMapping("/knowledge/lessons")
    @ResponseBody
    public AjaxResult knowledgeLessons() {
        StudyLesson query = new StudyLesson();
        query.setStatus("0");
        return AjaxResult.success(lessonService.selectStudyLessonList(query));
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/knowledge/{id}")
    @ResponseBody
    public AjaxResult knowledgeDetail(@PathVariable("id") Long id) {
        StudyKnowledgePoint kp = new StudyKnowledgePoint();
        kp.setId(id);
        List<StudyKnowledgePoint> list = knowledgePointService.selectStudyKnowledgePointList(kp);
        if (list != null && !list.isEmpty()) {
            return AjaxResult.success(list.get(0));
        }
        return AjaxResult.error("未找到相关知识点");
    }

    /**
     * 获取题目列表（分页）
     */
    @GetMapping("/list")
    @ResponseBody
    public TableDataInfo list(StudyExercise exercise) {
        startPage();
        List<StudyExercise> list = exerciseService.selectStudyExerciseList(exercise);
        return getDataTable(list);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public AjaxResult detail(@PathVariable("id") Long id) {
        StudyExercise exercise = exerciseService.selectStudyExerciseById(id);
        if (exercise != null) {
            return AjaxResult.success(exercise);
        }
        return AjaxResult.error("未找到相关题目");
    }

    @Autowired
    private IStudyExerciseRecordService exerciseRecordService;

    /**
     * 提交答案
     */
    @PostMapping("/submit")
    @ResponseBody
    public AjaxResult submit(@RequestBody List<StudyExerciseRecord> records) {
        SysUser user = getSysUser();
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        
        JSONObject result = new JSONObject();
        JSONArray details = new JSONArray();
        int correctCount = 0;

        for (StudyExerciseRecord record : records) {
            record.setUserId(user.getUserId());
            StudyExercise exercise = exerciseService.selectStudyExerciseById(record.getExerciseId());
            
            JSONObject detail = new JSONObject();
            detail.put("exerciseId", record.getExerciseId());
            
            if (exercise != null) {
                detail.put("correctAnswer", exercise.getAnswer());
                detail.put("explanation", exercise.getExplanation());
                
                if (exercise.getAnswer().equals(record.getUserAnswer())) {
                    record.setIsCorrect(1);
                    detail.put("isCorrect", true);
                    correctCount++;
                } else {
                    record.setIsCorrect(0);
                    detail.put("isCorrect", false);
                    wrongAnswerService.addWrongAnswer(user.getUserId(), record.getExerciseId());
                }
            }
            
            exerciseRecordService.insertStudyExerciseRecord(record);
            details.add(detail);
        }
        
        result.put("correctCount", correctCount);
        result.put("totalCount", records.size());
        result.put("details", details);
        
        return AjaxResult.success("提交成功", result);
    }

    /**
     * 提交课程配套习题答案（study_lesson_exercise）
     * 入库策略：
     * 1. study_exercise_record.exercise_id 存负数（-lessonExerciseId）避免与题库ID冲突
     * 2. study_wrong_answer.exercise_id 同样存负数，错题本可识别并回查课程习题表
     */
    @PostMapping("/submit-lesson")
    @ResponseBody
    public AjaxResult submitLesson(@RequestBody List<Map<String, Object>> records) {
        SysUser user = getSysUser();
        if (user == null) {
            return AjaxResult.error("请先登录");
        }
        if (records == null || records.isEmpty()) {
            return AjaxResult.error("提交内容为空");
        }

        JSONObject result = new JSONObject();
        JSONArray details = new JSONArray();
        int correctCount = 0;

        for (Map<String, Object> item : records) {
            Long lessonExerciseId = toLong(item.get("exerciseId"));
            String userAnswer = item.get("userAnswer") == null ? "" : String.valueOf(item.get("userAnswer")).trim();
            if (lessonExerciseId == null) {
                continue;
            }

            StudyLessonExercise exercise = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
            JSONObject detail = new JSONObject();
            detail.put("exerciseId", lessonExerciseId);
            detail.put("userAnswer", userAnswer);

            Integer isCorrect = 0;
            String correctAnswer = "";
            String analysis = "";
            if (exercise != null) {
                correctAnswer = exercise.getAnswer() == null ? "" : exercise.getAnswer().trim();
                analysis = exercise.getAnalysis();
                isCorrect = judgeLessonAnswer(exercise.getExerciseType(), correctAnswer, userAnswer) ? 1 : 0;
            }

            Long storedId = toLessonRecordId(lessonExerciseId);
            StudyExerciseRecord record = new StudyExerciseRecord();
            record.setUserId(user.getUserId());
            record.setExerciseId(storedId);
            record.setUserAnswer(userAnswer);
            record.setIsCorrect(isCorrect);
            exerciseRecordService.insertStudyExerciseRecord(record);

            if (isCorrect == 0) {
                wrongAnswerService.addWrongAnswer(user.getUserId(), storedId);
            } else {
                correctCount++;
            }

            detail.put("isCorrect", isCorrect == 1);
            detail.put("correctAnswer", correctAnswer);
            detail.put("explanation", analysis == null ? "" : analysis);
            details.add(detail);
        }

        result.put("correctCount", correctCount);
        result.put("totalCount", details.size());
        result.put("details", details);
        return AjaxResult.success("提交成功", result);
    }

    /**
     * 错题本
     */
    @GetMapping("/wrong-answer")
    public String wrongAnswer(ModelMap mmap) {
        SysUser user = getSysUser();
        List<StudyWrongAnswer> list = java.util.Collections.emptyList();
        if (user != null) {
            StudyWrongAnswer wrongAnswer = new StudyWrongAnswer();
            wrongAnswer.setUserId(user.getUserId());
            list = purgeDeletedWrongAnswers(wrongAnswerService.selectStudyWrongAnswerList(wrongAnswer));
        }
        mmap.put("wrongAnswerList", list);
        mmap.put("needLogin", user == null);
        return "web/wrong_answer";
    }

    /**
     * 错题分页列表（含题目内容）
     */
    @GetMapping("/wrong-answer/list")
    @ResponseBody
    public TableDataInfo wrongAnswerList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        SysUser user = getSysUser();
        if (user == null) {
            return getDataTable(java.util.Collections.emptyList());
        }
        PageHelper.startPage(pageNum, 5);
        StudyWrongAnswer filter = new StudyWrongAnswer();
        filter.setUserId(user.getUserId());
        List<StudyWrongAnswer> wrongList = purgeDeletedWrongAnswers(wrongAnswerService.selectStudyWrongAnswerList(filter));
        List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (StudyWrongAnswer wa : wrongList) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", wa.getId());
            item.put("exerciseId", wa.getExerciseId());
            item.put("wrongCount", wa.getWrongCount());
            item.put("lastWrongTime", wa.getLastWrongTime());
            if (isLessonRecordId(wa.getExerciseId())) {
                Long lessonExerciseId = toLessonExerciseId(wa.getExerciseId());
                StudyLessonExercise ex = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
                if (ex != null) {
                    item.put("content", ex.getTitle());
                    item.put("type", ex.getExerciseType());
                    item.put("answer", ex.getAnswer());
                    item.put("explanation", ex.getAnalysis());
                    item.put("knowledgePointId", null);
                }
            } else {
                StudyExercise ex = exerciseService.selectStudyExerciseById(wa.getExerciseId());
                if (ex != null) {
                    item.put("content", ex.getContent());
                    item.put("type", ex.getType());
                    item.put("answer", ex.getAnswer());
                    item.put("explanation", ex.getExplanation());
                    item.put("knowledgePointId", ex.getKnowledgePointId());
                }
            }
            rows.add(item);
        }
        return getDataTable(rows);
    }

    @GetMapping("/wrong-answer/suggestion")
    @ResponseBody
    public AjaxResult wrongAnswerSuggestion() {
        SysUser user = getSysUser();
        if (user == null) {
            return AjaxResult.error("请先登录");
        }

        StudyWrongAnswer filter = new StudyWrongAnswer();
        filter.setUserId(user.getUserId());
        List<StudyWrongAnswer> wrongList = purgeDeletedWrongAnswers(wrongAnswerService.selectStudyWrongAnswerList(filter));
        if (wrongList == null || wrongList.isEmpty()) {
            return AjaxResult.success("暂无错题记录，建议从任意知识点进入练习，系统会自动统计薄弱点。");
        }

        List<StudyWrongAnswer> topList = wrongList.stream()
                .sorted(Comparator.comparing((StudyWrongAnswer w) -> w.getWrongCount() == null ? 0 : w.getWrongCount()).reversed())
                .limit(12)
                .collect(Collectors.toList());

        StringBuilder ctx = new StringBuilder();
        for (StudyWrongAnswer wa : topList) {
            String kpName = "";
            String question = "";
            String answer = "";
            String explanation = "";
            if (isLessonRecordId(wa.getExerciseId())) {
                Long lessonExerciseId = toLessonExerciseId(wa.getExerciseId());
                StudyLessonExercise ex = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
                if (ex == null) {
                    continue;
                }
                question = ex.getTitle();
                answer = ex.getAnswer();
                explanation = ex.getAnalysis();
            } else {
                StudyExercise ex = exerciseService.selectStudyExerciseById(wa.getExerciseId());
                if (ex == null) {
                    continue;
                }
                question = ex.getContent();
                answer = ex.getAnswer();
                explanation = ex.getExplanation();
                if (ex.getKnowledgePointId() != null) {
                    StudyKnowledgePoint kpQuery = new StudyKnowledgePoint();
                    kpQuery.setId(ex.getKnowledgePointId());
                    List<StudyKnowledgePoint> kpList = knowledgePointService.selectStudyKnowledgePointList(kpQuery);
                    if (kpList != null && !kpList.isEmpty()) {
                        kpName = kpList.get(0).getName();
                    }
                }
            }
            ctx.append("【知识点】").append(kpName == null ? "" : kpName).append("\n");
            ctx.append("【题目】").append(question == null ? "" : question).append("\n");
            ctx.append("【正确答案】").append(answer == null ? "" : answer).append("\n");
            ctx.append("【解析】").append(explanation == null ? "" : explanation).append("\n");
            ctx.append("【错误次数】").append(wa.getWrongCount() == null ? 0 : wa.getWrongCount()).append("\n");
            ctx.append("----\n");
        }

        String suggestion;
        try {
            suggestion = tongYiAI.generateWrongAnswerSuggestion(ctx.toString());
        } catch (Exception e) {
            logger.error("生成千问学习建议失败", e);
            return AjaxResult.error("千问建议生成失败，请稍后重试");
        }
        if (suggestion != null) {
            suggestion = suggestion.replace("```", "").trim();
        }
        return AjaxResult.success(suggestion);
    }

    /**
     * 自动清理已删除题目的错题记录，避免前端显示“题目已删除”。
     */
    private List<StudyWrongAnswer> purgeDeletedWrongAnswers(List<StudyWrongAnswer> wrongList) {
        if (wrongList == null || wrongList.isEmpty()) {
            return wrongList == null ? java.util.Collections.emptyList() : wrongList;
        }
        List<Long> deleteIds = new ArrayList<>();
        List<StudyWrongAnswer> validList = new ArrayList<>();
        for (StudyWrongAnswer wa : wrongList) {
            boolean exists;
            if (isLessonRecordId(wa.getExerciseId())) {
                Long lessonExerciseId = toLessonExerciseId(wa.getExerciseId());
                exists = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId) != null;
            } else {
                exists = exerciseService.selectStudyExerciseById(wa.getExerciseId()) != null;
            }
            if (exists) {
                validList.add(wa);
            } else if (wa.getId() != null) {
                deleteIds.add(wa.getId());
            }
        }
        if (!deleteIds.isEmpty()) {
            wrongAnswerService.deleteStudyWrongAnswerByIds(deleteIds);
        }
        return validList;
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public AjaxResult detailWithLessonFallback(@PathVariable("id") Long id) {
        if (id == null) {
            return AjaxResult.error("参数错误");
        }
        if (isLessonRecordId(id)) {
            Long lessonExerciseId = toLessonExerciseId(id);
            StudyLessonExercise ex = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
            if (ex == null) {
                return AjaxResult.error("未找到相关题目");
            }
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", id);
            data.put("content", ex.getTitle());
            data.put("answer", ex.getAnswer());
            data.put("explanation", ex.getAnalysis());
            data.put("type", ex.getExerciseType());
            return AjaxResult.success(data);
        }
        StudyExercise exercise = exerciseService.selectStudyExerciseById(id);
        if (exercise != null) {
            return AjaxResult.success(exercise);
        }
        return AjaxResult.error("未找到相关题目");
    }

    private boolean judgeLessonAnswer(String type, String expected, String actual) {
        String exp = expected == null ? "" : expected.trim();
        String ans = actual == null ? "" : actual.trim();
        if ("4".equals(type)) {
            return normalizeText(exp).equals(normalizeText(ans));
        }
        if ("3".equals(type)) {
            return normalizeTf(exp).equals(normalizeTf(ans));
        }
        if ("1".equals(type) || "2".equals(type)) {
            return normalizeChoice(exp).equals(normalizeChoice(ans));
        }
        return normalizeText(exp).equals(normalizeText(ans));
    }

    private String normalizeTf(String s) {
        String v = normalizeText(s);
        if ("true".equals(v) || "t".equals(v) || "1".equals(v) || "对".equals(v) || "正确".equals(v) || "√".equals(v)) {
            return "对";
        }
        if ("false".equals(v) || "f".equals(v) || "0".equals(v) || "错".equals(v) || "错误".equals(v) || "×".equals(v)) {
            return "错";
        }
        return v;
    }

    private String normalizeChoice(String s) {
        String v = (s == null ? "" : s).toUpperCase().replaceAll("[，、;\\s]+", ",");
        if (v.matches("^[A-Z]+$") && v.length() > 1) {
            v = String.join(",", v.split(""));
        }
        String[] parts = v.split(",");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.trim().isEmpty()) {
                list.add(p.trim());
            }
        }
        list.sort(String::compareTo);
        return String.join(",", list);
    }

    private String normalizeText(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("\\s+", "");
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLessonRecordId(Long lessonExerciseId) {
        return lessonExerciseId == null ? null : -Math.abs(lessonExerciseId);
    }

    private boolean isLessonRecordId(Long exerciseId) {
        return exerciseId != null && exerciseId < 0;
    }

    private Long toLessonExerciseId(Long exerciseId) {
        return exerciseId == null ? null : Math.abs(exerciseId);
    }
}
