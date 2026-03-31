package com.jy.study.web.controller.web;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import com.jy.study.web.util.AnswerJudgeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

        Map<Long, StudyExercise> exerciseMap = new LinkedHashMap<>();
        for (StudyExerciseRecord record : records) {
            if (record == null || record.getExerciseId() == null) {
                continue;
            }
            exerciseMap.put(record.getExerciseId(), exerciseService.selectStudyExerciseById(record.getExerciseId()));
        }
        Map<Long, ShortAnswerJudgeDecision> aiJudgeMap = judgeStudyShortAnswersWithAi(records, exerciseMap);
        
        JSONObject result = new JSONObject();
        JSONArray details = new JSONArray();
        int correctCount = 0;

        for (StudyExerciseRecord record : records) {
            if (record == null) {
                continue;
            }
            record.setUserId(user.getUserId());
            StudyExercise exercise = exerciseMap.get(record.getExerciseId());
            String userAnswer = record.getUserAnswer() == null ? "" : record.getUserAnswer().trim();
            record.setUserAnswer(userAnswer);
            
            JSONObject detail = new JSONObject();
            detail.put("exerciseId", record.getExerciseId());
            detail.put("userAnswer", userAnswer);
            detail.put("correctAnswer", "");
            detail.put("explanation", "");
            detail.put("isCorrect", false);
            
            if (exercise != null) {
                detail.put("correctAnswer", exercise.getAnswer());
                String explanation = exercise.getExplanation();

                boolean isCorrect = AnswerJudgeUtils.judgeStudyExerciseAnswer(
                        exercise.getType(),
                        exercise.getAnswer(),
                        userAnswer,
                        exercise.getOptions()
                );
                ShortAnswerJudgeDecision aiDecision = aiJudgeMap.get(record.getExerciseId());
                if (!isCorrect && aiDecision != null) {
                    isCorrect = aiDecision.correct;
                    detail.put("gradingMethod", "ai");
                    detail.put("aiScore", aiDecision.score);
                    detail.put("aiJudgeReason", aiDecision.reason);
                    explanation = mergeExplanationWithAiReason(explanation, aiDecision.reason);
                }
                detail.put("explanation", explanation);
                if (isCorrect) {
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

        Map<Long, StudyLessonExercise> exerciseMap = new LinkedHashMap<>();
        for (Map<String, Object> item : records) {
            Long lessonExerciseId = toLong(item.get("exerciseId"));
            if (lessonExerciseId == null) {
                continue;
            }
            exerciseMap.put(lessonExerciseId, lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId));
        }
        Map<Long, ShortAnswerJudgeDecision> aiJudgeMap = judgeLessonShortAnswersWithAi(records, exerciseMap);

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
                isCorrect = AnswerJudgeUtils.judgeLessonExerciseAnswer(
                        exercise.getExerciseType(),
                        correctAnswer,
                        userAnswer,
                        exercise.getOptions()
                ) ? 1 : 0;
                ShortAnswerJudgeDecision aiDecision = aiJudgeMap.get(lessonExerciseId);
                if (isCorrect == 0 && aiDecision != null) {
                    isCorrect = aiDecision.correct ? 1 : 0;
                    detail.put("gradingMethod", "ai");
                    detail.put("aiScore", aiDecision.score);
                    detail.put("aiJudgeReason", aiDecision.reason);
                    analysis = mergeExplanationWithAiReason(analysis, aiDecision.reason);
                }
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
        mmap.put("needLogin", user == null);
        return "web/wrong_answer";
    }

    /**
     * 错题分页列表（含题目内容）
     */
    @GetMapping("/wrong-answer/list")
    @ResponseBody
    public TableDataInfo wrongAnswerList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                         @RequestParam(value = "contentType", required = false) String contentType,
                                         @RequestParam(value = "contentId", required = false) Long contentId) {
        SysUser user = getSysUser();
        if (user == null) {
            return getDataTable(java.util.Collections.emptyList());
        }
        wrongAnswerService.purgeInvalidWrongAnswers();
        PageHelper.startPage(pageNum, pageSize);
        StudyWrongAnswer filter = new StudyWrongAnswer();
        filter.setUserId(user.getUserId());
        filter.setSourceType(contentType);
        filter.setSourceId(contentId);
        List<StudyWrongAnswer> wrongList = wrongAnswerService.selectStudyWrongAnswerList(filter);
        List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (StudyWrongAnswer wa : wrongList) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", wa.getId());
            item.put("exerciseId", wa.getExerciseId());
            item.put("wrongCount", wa.getWrongCount());
            item.put("lastWrongTime", wa.getLastWrongTime());
            item.put("content", wa.getContent());
            item.put("type", wa.getType());
            item.put("answer", wa.getAnswer());
            item.put("explanation", wa.getExplanation());
            item.put("knowledgePointId", wa.getKnowledgePointId());
            item.put("knowledgePointName", wa.getKnowledgePointName());
            item.put("sourceType", wa.getSourceType());
            item.put("sourceId", wa.getSourceId());
            item.put("sourceTitle", wa.getSourceTitle());
            item.put("typeName", resolveWrongAnswerTypeName(wa));
            rows.add(item);
        }
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(0);
        rspData.setRows(rows);
        rspData.setTotal(new PageInfo<>(wrongList).getTotal());
        return rspData;
    }

    @GetMapping("/wrong-answer/suggestion")
    @ResponseBody
    public AjaxResult wrongAnswerSuggestion(@RequestParam(value = "contentType", required = false) String contentType,
                                            @RequestParam(value = "contentId", required = false) Long contentId) {
        SysUser user = getSysUser();
        if (user == null) {
            return AjaxResult.error("请先登录");
        }

        wrongAnswerService.purgeInvalidWrongAnswers();
        StudyWrongAnswer filter = new StudyWrongAnswer();
        filter.setUserId(user.getUserId());
        filter.setSourceType(contentType);
        filter.setSourceId(contentId);
        List<StudyWrongAnswer> wrongList = wrongAnswerService.selectStudyWrongAnswerList(filter);
        if (wrongList == null || wrongList.isEmpty()) {
            return AjaxResult.success("暂无错题记录，建议从任意知识点进入练习，系统会自动统计薄弱点。");
        }

        List<StudyWrongAnswer> topList = wrongList.stream()
                .sorted(Comparator.comparing((StudyWrongAnswer w) -> w.getWrongCount() == null ? 0 : w.getWrongCount()).reversed())
                .limit(12)
                .collect(Collectors.toList());

        List<WrongAnswerSuggestionItem> suggestionItems = buildWrongAnswerSuggestionItems(topList);
        if (suggestionItems.isEmpty()) {
            return AjaxResult.success("当前错题缺少可分析的题目信息，建议先重新完成几道练习后再生成学习建议。");
        }

        String ctx = buildWrongAnswerSuggestionContext(suggestionItems);

        String suggestion;
        try {
            suggestion = tongYiAI.generateWrongAnswerSuggestion(ctx);
        } catch (Exception e) {
            logger.error("生成千问学习建议失败", e);
            suggestion = "";
        }

        suggestion = sanitizeSuggestionText(suggestion);
        if (isBlank(suggestion)) {
            logger.warn("千问学习建议为空，已切换为本地兜底建议，userId={}", user.getUserId());
            suggestion = buildFallbackWrongAnswerSuggestion(suggestionItems);
        }
        return AjaxResult.success(suggestion);
    }

    /**
     * 自动清理已删除题目的错题记录，避免前端显示“题目已删除”。
     */
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

    private Map<Long, ShortAnswerJudgeDecision> judgeStudyShortAnswersWithAi(List<StudyExerciseRecord> records,
                                                                             Map<Long, StudyExercise> exerciseMap) {
        List<ShortAnswerJudgeCandidate> candidates = new ArrayList<>();
        for (StudyExerciseRecord record : records) {
            if (record == null || record.getExerciseId() == null) {
                continue;
            }
            StudyExercise exercise = exerciseMap.get(record.getExerciseId());
            if (exercise == null || !"4".equals(exercise.getType())) {
                continue;
            }
            String userAnswer = safeTrim(record.getUserAnswer());
            if (isBlank(userAnswer) || isBlank(exercise.getAnswer())) {
                continue;
            }
            boolean ruleCorrect = AnswerJudgeUtils.judgeStudyExerciseAnswer(
                    exercise.getType(),
                    exercise.getAnswer(),
                    userAnswer,
                    exercise.getOptions()
            );
            if (ruleCorrect) {
                continue;
            }
            candidates.add(new ShortAnswerJudgeCandidate(
                    record.getExerciseId(),
                    exercise.getSubjectName(),
                    exercise.getGradeName(),
                    exercise.getContent(),
                    exercise.getAnswer(),
                    exercise.getExplanation(),
                    userAnswer
            ));
        }
        return judgeShortAnswerCandidatesWithAi(candidates);
    }

    private Map<Long, ShortAnswerJudgeDecision> judgeLessonShortAnswersWithAi(List<Map<String, Object>> records,
                                                                              Map<Long, StudyLessonExercise> exerciseMap) {
        List<ShortAnswerJudgeCandidate> candidates = new ArrayList<>();
        for (Map<String, Object> item : records) {
            Long lessonExerciseId = toLong(item.get("exerciseId"));
            if (lessonExerciseId == null) {
                continue;
            }
            StudyLessonExercise exercise = exerciseMap.get(lessonExerciseId);
            if (exercise == null || !"4".equals(exercise.getExerciseType())) {
                continue;
            }
            String userAnswer = item.get("userAnswer") == null ? "" : String.valueOf(item.get("userAnswer")).trim();
            if (isBlank(userAnswer) || isBlank(exercise.getAnswer())) {
                continue;
            }
            boolean ruleCorrect = AnswerJudgeUtils.judgeLessonExerciseAnswer(
                    exercise.getExerciseType(),
                    exercise.getAnswer(),
                    userAnswer,
                    exercise.getOptions()
            );
            if (ruleCorrect) {
                continue;
            }
            candidates.add(new ShortAnswerJudgeCandidate(
                    lessonExerciseId,
                    exercise.getSubjectName(),
                    exercise.getGradeName(),
                    exercise.getTitle(),
                    exercise.getAnswer(),
                    exercise.getAnalysis(),
                    userAnswer
            ));
        }
        return judgeShortAnswerCandidatesWithAi(candidates);
    }

    private Map<Long, ShortAnswerJudgeDecision> judgeShortAnswerCandidatesWithAi(List<ShortAnswerJudgeCandidate> candidates) {
        Map<Long, ShortAnswerJudgeDecision> result = new HashMap<>();
        if (candidates == null || candidates.isEmpty()) {
            return result;
        }

        JSONArray requestItems = new JSONArray();
        for (ShortAnswerJudgeCandidate candidate : candidates) {
            JSONObject item = new JSONObject();
            item.put("qid", String.valueOf(candidate.exerciseId));
            item.put("subject", safeTrim(candidate.subjectName));
            item.put("grade", safeTrim(candidate.gradeName));
            item.put("question", safeTrim(candidate.question));
            item.put("referenceAnswer", safeTrim(candidate.referenceAnswer));
            item.put("analysis", safeTrim(candidate.analysis));
            item.put("userAnswer", safeTrim(candidate.userAnswer));
            requestItems.add(item);
        }

        try {
            JSONArray aiResults = tongYiAI.judgeShortAnswerBatch(requestItems);
            for (int i = 0; i < aiResults.size(); i++) {
                JSONObject item = aiResults.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                Long exerciseId = toLong(item.get("qid"));
                if (exerciseId == null) {
                    continue;
                }
                ShortAnswerJudgeDecision decision = new ShortAnswerJudgeDecision();
                decision.score = normalizeAiScore(item.getInteger("score"));
                decision.correct = resolveAiCorrect(item.get("isCorrect"), decision.score);
                decision.reason = normalizeAiReason(item.getString("reason"));
                result.put(exerciseId, decision);
            }
        } catch (Exception e) {
            logger.error("AI 简答题辅助判分失败", e);
        }
        return result;
    }

    private boolean resolveAiCorrect(Object rawValue, Integer score) {
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue != null) {
            String value = String.valueOf(rawValue).trim().toLowerCase();
            if ("true".equals(value) || "1".equals(value) || "yes".equals(value) || "y".equals(value) || "是".equals(value)) {
                return true;
            }
            if ("false".equals(value) || "0".equals(value) || "no".equals(value) || "n".equals(value) || "否".equals(value)) {
                return false;
            }
        }
        return score != null && score >= 60;
    }

    private Integer normalizeAiScore(Integer score) {
        if (score == null) {
            return null;
        }
        if (score < 0) {
            return 0;
        }
        if (score > 100) {
            return 100;
        }
        return score;
    }

    private String mergeExplanationWithAiReason(String explanation, String aiReason) {
        String base = safeTrim(explanation);
        String reason = normalizeAiReason(aiReason);
        if (reason.isEmpty()) {
            return base;
        }
        if (base.isEmpty()) {
            return "AI判分说明：" + reason;
        }
        return base + " AI判分说明：" + reason;
    }

    private String normalizeAiReason(String reason) {
        if (reason == null) {
            return "";
        }
        return reason.replaceAll("<[^>]+>", "")
                .replaceAll("[\\r\\n]+", " ")
                .trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private List<WrongAnswerSuggestionItem> buildWrongAnswerSuggestionItems(List<StudyWrongAnswer> wrongList) {
        List<WrongAnswerSuggestionItem> items = new ArrayList<>();
        if (wrongList == null || wrongList.isEmpty()) {
            return items;
        }
        for (StudyWrongAnswer wa : wrongList) {
            if (wa == null || wa.getExerciseId() == null) {
                continue;
            }
            WrongAnswerSuggestionItem item = new WrongAnswerSuggestionItem();
            item.exerciseId = wa.getExerciseId();
            item.wrongCount = wa.getWrongCount() == null ? 0 : wa.getWrongCount();
            item.lastWrongTime = wa.getLastWrongTime() == null ? "" : wa.getLastWrongTime().toString();
            item.question = safeTrim(wa.getContent());
            item.answer = safeTrim(wa.getAnswer());
            item.explanation = safeTrim(wa.getExplanation());
            item.knowledgePoint = safeTrim(wa.getKnowledgePointName());
            item.subjectName = "";
            item.gradeName = "";
            item.typeName = resolveWrongAnswerTypeName(wa);
            item.sourceName = safeTrim(wa.getSourceTitle());

            if (!isBlank(item.question) && !isBlank(item.answer)) {
                items.add(item);
                continue;
            }

            if (isLessonRecordId(wa.getExerciseId())) {
                Long lessonExerciseId = toLessonExerciseId(wa.getExerciseId());
                StudyLessonExercise ex = lessonExerciseService.selectStudyLessonExerciseById(lessonExerciseId);
                if (ex == null) {
                    continue;
                }
                item.question = safeTrim(ex.getTitle());
                item.answer = safeTrim(ex.getAnswer());
                item.explanation = safeTrim(ex.getAnalysis());
                item.knowledgePoint = safeTrim(ex.getLessonTitle());
                item.subjectName = safeTrim(ex.getSubjectName());
                item.gradeName = safeTrim(ex.getGradeName());
                item.typeName = getLessonExerciseTypeName(ex.getExerciseType());
                item.sourceName = safeTrim(ex.getLessonTitle());
            } else {
                StudyExercise ex = exerciseService.selectStudyExerciseById(wa.getExerciseId());
                if (ex == null) {
                    continue;
                }
                item.question = safeTrim(ex.getContent());
                item.answer = safeTrim(ex.getAnswer());
                item.explanation = safeTrim(ex.getExplanation());
                item.subjectName = safeTrim(ex.getSubjectName());
                item.gradeName = safeTrim(ex.getGradeName());
                item.typeName = getStudyExerciseTypeName(ex.getType());
                item.sourceName = "";
                if (ex.getKnowledgePointId() != null) {
                    StudyKnowledgePoint kpQuery = new StudyKnowledgePoint();
                    kpQuery.setId(ex.getKnowledgePointId());
                    List<StudyKnowledgePoint> kpList = knowledgePointService.selectStudyKnowledgePointList(kpQuery);
                    if (kpList != null && !kpList.isEmpty()) {
                        item.knowledgePoint = safeTrim(kpList.get(0).getName());
                    }
                }
            }
            items.add(item);
        }
        return items;
    }

    private String resolveWrongAnswerTypeName(StudyWrongAnswer wa) {
        if (wa == null) {
            return "未知题型";
        }
        if ("lesson".equalsIgnoreCase(safeTrim(wa.getSourceType())) || isLessonRecordId(wa.getExerciseId())) {
            return getLessonExerciseTypeName(wa.getType());
        }
        return getStudyExerciseTypeName(wa.getType());
    }

    private String buildWrongAnswerSuggestionContext(List<WrongAnswerSuggestionItem> items) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("【总体情况】\n");
        ctx.append("错题数量：").append(items.size()).append("\n");

        Map<String, Integer> focusMap = buildFocusCountMap(items);
        if (!focusMap.isEmpty()) {
            ctx.append("高频薄弱点：").append(joinTopLabels(focusMap, 3)).append("\n");
        }
        ctx.append("\n【错题详情】\n");

        for (WrongAnswerSuggestionItem item : items) {
            ctx.append("【题目ID】").append(item.exerciseId).append("\n");
            ctx.append("【学科】").append(item.subjectName).append("\n");
            ctx.append("【年级】").append(item.gradeName).append("\n");
            ctx.append("【来源】").append(item.sourceName).append("\n");
            ctx.append("【知识点】").append(item.knowledgePoint).append("\n");
            ctx.append("【题型】").append(item.typeName).append("\n");
            ctx.append("【题目】").append(item.question).append("\n");
            ctx.append("【正确答案】").append(item.answer).append("\n");
            ctx.append("【解析】").append(item.explanation).append("\n");
            ctx.append("【错误次数】").append(item.wrongCount).append("\n");
            ctx.append("【最近答错时间】").append(item.lastWrongTime).append("\n");
            ctx.append("----\n");
        }
        return ctx.toString();
    }

    private Map<String, Integer> buildFocusCountMap(List<WrongAnswerSuggestionItem> items) {
        Map<String, Integer> counter = new LinkedHashMap<>();
        for (WrongAnswerSuggestionItem item : items) {
            String label = !isBlank(item.knowledgePoint) ? item.knowledgePoint
                    : (!isBlank(item.subjectName) ? item.subjectName + item.typeName : item.typeName);
            if (isBlank(label)) {
                label = "综合基础";
            }
            counter.put(label, counter.getOrDefault(label, 0) + Math.max(1, item.wrongCount));
        }
        return counter.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private String joinTopLabels(Map<String, Integer> counter, int limit) {
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            labels.add(entry.getKey() + "（" + entry.getValue() + "次）");
            index++;
            if (index >= limit) {
                break;
            }
        }
        return String.join("、", labels);
    }

    private String sanitizeSuggestionText(String suggestion) {
        if (suggestion == null) {
            return "";
        }
        return suggestion.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String buildFallbackWrongAnswerSuggestion(List<WrongAnswerSuggestionItem> items) {
        Map<String, Integer> focusMap = buildFocusCountMap(items);
        String focusText = focusMap.isEmpty() ? "综合基础" : joinTopLabels(focusMap, 3);
        WrongAnswerSuggestionItem topItem = items.get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("现状诊断：你当前累计有 ").append(items.size()).append(" 道高频错题，主要薄弱点集中在 ")
                .append(focusText).append("。")
                .append("其中最需要优先处理的一题是“").append(safeTrim(topItem.question)).append("”，")
                .append("这类题已经重复出错 ").append(topItem.wrongCount).append(" 次。\n\n");

        sb.append("优先复习知识点：先回到 ")
                .append(isBlank(topItem.knowledgePoint) ? "对应教材与解析" : topItem.knowledgePoint)
                .append("，把标准答案、题目关键词和解析里的因果关系重新整理一遍。");
        if (!isBlank(topItem.subjectName)) {
            sb.append("本轮建议重点复习 ").append(topItem.subjectName);
            if (!isBlank(topItem.gradeName)) {
                sb.append(" ").append(topItem.gradeName);
            }
            sb.append(" 相关基础内容。");
        }
        sb.append("\n\n");

        sb.append("建议练习计划：接下来 3 天每天安排 20 到 30 分钟复盘。")
                .append("先精读 2 道错题解析，再口头复述正确思路，最后重新做 3 道同类型题。")
                .append("如果是简答题，每次作答都按“结论 + 关键词 + 简短依据”三步写满；")
                .append("如果是选择或判断题，要把干扰项为什么错也一并写出来。\n\n");

        sb.append("注意事项：不要只看答案，要先自己回忆再核对；")
                .append("同一题连续错 2 次以上时，说明不是粗心而是知识点没吃透，必须回教材或课程原文重学。")
                .append("做完新练习后，把仍然出错的题继续加入错题本，下一次优先复盘。");
        return sb.toString();
    }

    private String getStudyExerciseTypeName(String type) {
        if ("1".equals(type)) {
            return "选择题";
        }
        if ("2".equals(type)) {
            return "填空题";
        }
        if ("3".equals(type)) {
            return "判断题";
        }
        if ("4".equals(type)) {
            return "简答题";
        }
        return "未知题型";
    }

    private String getLessonExerciseTypeName(String type) {
        if ("1".equals(type)) {
            return "单选题";
        }
        if ("2".equals(type)) {
            return "多选题";
        }
        if ("3".equals(type)) {
            return "判断题";
        }
        if ("4".equals(type)) {
            return "简答题";
        }
        return "未知题型";
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

    private static class ShortAnswerJudgeCandidate {
        private final Long exerciseId;
        private final String subjectName;
        private final String gradeName;
        private final String question;
        private final String referenceAnswer;
        private final String analysis;
        private final String userAnswer;

        private ShortAnswerJudgeCandidate(Long exerciseId, String subjectName, String gradeName, String question,
                                          String referenceAnswer, String analysis, String userAnswer) {
            this.exerciseId = exerciseId;
            this.subjectName = subjectName;
            this.gradeName = gradeName;
            this.question = question;
            this.referenceAnswer = referenceAnswer;
            this.analysis = analysis;
            this.userAnswer = userAnswer;
        }
    }

    private static class ShortAnswerJudgeDecision {
        private boolean correct;
        private Integer score;
        private String reason;
    }

    private static class WrongAnswerSuggestionItem {
        private Long exerciseId;
        private String subjectName = "";
        private String gradeName = "";
        private String sourceName = "";
        private String knowledgePoint = "";
        private String typeName = "";
        private String question = "";
        private String answer = "";
        private String explanation = "";
        private Integer wrongCount = 0;
        private String lastWrongTime = "";
    }
}
