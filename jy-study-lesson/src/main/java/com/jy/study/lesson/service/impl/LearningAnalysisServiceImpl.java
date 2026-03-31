package com.jy.study.lesson.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jy.study.common.ai.TongYiAI;
import com.jy.study.lesson.domain.StudyExerciseRecord;
import com.jy.study.lesson.service.IStudyExerciseRecordService;
import com.jy.study.lesson.service.ILearningAnalysisService;
import com.jy.study.lesson.domain.LearningAnalysis;
import com.jy.study.lesson.domain.LearningPath;
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.service.IStudyExerciseService;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.lesson.service.IStudyKnowledgePointService;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习分析服务实现
 */
@Service
public class LearningAnalysisServiceImpl implements ILearningAnalysisService {

    @Autowired
    private IStudyExerciseRecordService exerciseRecordService;

    @Autowired
    private IStudyExerciseService exerciseService;

    @Autowired
    private IStudyKnowledgePointService knowledgePointService;

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private TongYiAI tongYiAI;

    /**
     * 分析用户学习情况
     */
    @Override
    public LearningAnalysis analyzeUserLearning(Long userId) {
        LearningAnalysis analysis = new LearningAnalysis();
        analysis.setUserId(userId);

        // 获取用户所有练习记录
        List<StudyExerciseRecord> records = exerciseRecordService.selectStudyExerciseRecordByUserId(userId);
        analysis.setTotalExercises(records.size());

        // 计算正确率
        long correctCount = records.stream().filter(record -> Integer.valueOf(1).equals(record.getIsCorrect())).count();
        double accuracy = records.isEmpty() ? 0 : (double) correctCount / records.size() * 100;
        analysis.setAccuracy(accuracy);

        // 分析薄弱知识点
        Map<Long, List<StudyExerciseRecord>> knowledgePointRecords = groupRecordsByKnowledgePoint(records);
        List<Map<String, Object>> weakPoints = identifyWeakPoints(knowledgePointRecords);
        analysis.setWeakPoints(weakPoints);

        // 分析学习进度
        Map<String, Object> progress = analyzeLearningProgress(userId);
        analysis.setProgress(progress);

        // 评估学习能力
        analysis.setLearningLevel(evaluateLearningLevel(accuracy, records.size()));

        return analysis;
    }

    /**
     * 生成个性化学习路径
     */
    @Override
    public LearningPath generateLearningPath(Long userId) {
        LearningPath path = new LearningPath();
        path.setUserId(userId);
        path.setCreateTime(new Date());
        path.setStatus("active");

        // 分析用户学习情况
        LearningAnalysis analysis = analyzeUserLearning(userId);

        // 先准备规则推荐，作为 AI 失败时的兜底
        List<Map<String, Object>> fallbackRecommendations = generateRecommendations(analysis);
        List<Map<String, Object>> recommendations = fallbackRecommendations;
        List<Map<String, Object>> steps = generatePathSteps(fallbackRecommendations, analysis);
        int estimatedDays = Math.max(7, Math.min(30, steps.size() * 2));

        // 调用大模型生成个性化路线
        try {
            String profileContext = buildUserProfileContext(userId, analysis, fallbackRecommendations);
            String aiResult = tongYiAI.generatePersonalizedLearningPath(profileContext);
            LearningPath aiPath = parseAiLearningPath(aiResult);
            if (aiPath != null && aiPath.getPathSteps() != null && !aiPath.getPathSteps().isEmpty()) {
                recommendations = aiPath.getRecommendations() == null || aiPath.getRecommendations().isEmpty()
                        ? fallbackRecommendations : aiPath.getRecommendations();
                steps = aiPath.getPathSteps();
                estimatedDays = aiPath.getEstimatedDays() == null ? estimatedDays : aiPath.getEstimatedDays();
            }
        } catch (Exception ignored) {
            // AI 异常时自动降级为规则推荐，避免页面空白
        }

        path.setRecommendations(recommendations);
        path.setPathSteps(steps);
        path.setEstimatedDays(Math.max(7, Math.min(90, estimatedDays)));

        return path;
    }

    /**
     * 按知识点分组记录
     */
    private Map<Long, List<StudyExerciseRecord>> groupRecordsByKnowledgePoint(List<StudyExerciseRecord> records) {
        Map<Long, List<StudyExerciseRecord>> result = new HashMap<>();
        Map<Long, StudyExercise> exerciseCache = new HashMap<>();

        for (StudyExerciseRecord record : records) {
            if (record.getExerciseId() == null) {
                continue;
            }
            StudyExercise exercise = exerciseCache.computeIfAbsent(record.getExerciseId(),
                    id -> exerciseService.selectStudyExerciseById(id));
            if (exercise != null && exercise.getKnowledgePointId() != null) {
                result.computeIfAbsent(exercise.getKnowledgePointId(), k -> new ArrayList<>())
                      .add(record);
            }
        }

        return result;
    }

    /**
     * 识别薄弱知识点
     */
    private List<Map<String, Object>> identifyWeakPoints(Map<Long, List<StudyExerciseRecord>> knowledgePointRecords) {
        List<Map<String, Object>> weakPoints = new ArrayList<>();

        for (Map.Entry<Long, List<StudyExerciseRecord>> entry : knowledgePointRecords.entrySet()) {
            Long knowledgePointId = entry.getKey();
            List<StudyExerciseRecord> records = entry.getValue();

            long correctCount = records.stream().filter(record -> Integer.valueOf(1).equals(record.getIsCorrect())).count();
            double accuracy = (double) correctCount / records.size() * 100;

            if (accuracy < 60) { // 正确率低于60%视为薄弱
                StudyKnowledgePoint knowledgePoint = knowledgePointService.selectStudyKnowledgePointById(knowledgePointId);
                if (knowledgePoint != null) {
                    Map<String, Object> weakPoint = new HashMap<>();
                    weakPoint.put("knowledgePointId", knowledgePointId);
                    weakPoint.put("knowledgePointName", knowledgePoint.getName());
                    weakPoint.put("accuracy", accuracy);
                    weakPoint.put("totalExercises", records.size());
                    weakPoints.add(weakPoint);
                }
            }
        }

        // 按正确率排序，最薄弱的在前
        weakPoints.sort(Comparator.comparingDouble(o -> (Double) o.get("accuracy")));
        return weakPoints;
    }

    /**
     * 分析学习进度
     */
    private Map<String, Object> analyzeLearningProgress(Long userId) {
        Map<String, Object> progress = new HashMap<>();

        // 计算完成的练习数
        List<StudyExerciseRecord> records = exerciseRecordService.selectStudyExerciseRecordByUserId(userId);
        progress.put("completedExercises", records.size());

        // 计算总练习数
        int totalExercises = exerciseService.selectStudyExerciseList(new StudyExercise()).size();
        progress.put("totalExercises", totalExercises);

        // 计算进度百分比
        double progressPercentage = totalExercises > 0 ? (double) records.size() / totalExercises * 100 : 0;
        progress.put("progressPercentage", progressPercentage);

        return progress;
    }

    /**
     * 评估学习能力水平
     */
    private String evaluateLearningLevel(double accuracy, int totalExercises) {
        if (totalExercises < 10) {
            return "初学者";
        } else if (accuracy >= 80) {
            return "优秀";
        } else if (accuracy >= 60) {
            return "良好";
        } else if (accuracy >= 40) {
            return "一般";
        } else {
            return "需要加强";
        }
    }

    /**
     * 生成推荐内容
     */
    private List<Map<String, Object>> generateRecommendations(LearningAnalysis analysis) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Set<String> dedup = new HashSet<>();

        // 基于薄弱知识点推荐
        List<Map<String, Object>> weakPoints = analysis.getWeakPoints();
        List<Map<String, Object>> topWeakPoints = weakPoints.stream().limit(3).collect(Collectors.toList());
        for (Map<String, Object> weakPoint : topWeakPoints) {
            Long knowledgePointId = (Long) weakPoint.get("knowledgePointId");
            String knowledgePointName = String.valueOf(weakPoint.get("knowledgePointName"));
            List<StudyLesson> relatedLessons = findRelatedLessons(knowledgePointId, knowledgePointName);

            for (StudyLesson lesson : relatedLessons) {
                String key = "lesson:" + lesson.getLessonId();
                if (dedup.contains(key)) {
                    continue;
                }
                dedup.add(key);
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("type", "lesson");
                recommendation.put("id", lesson.getLessonId());
                recommendation.put("title", lesson.getTitle());
                recommendation.put("reason", "针对薄弱知识点: " + weakPoint.get("knowledgePointName"));
                recommendation.put("priority", "high");
                recommendations.add(recommendation);
                if (recommendations.size() >= 10) {
                    return recommendations;
                }
            }
        }

        // 推荐相关练习
        List<StudyExercise> recommendedExercises = findRecommendedExercises(analysis);
        for (StudyExercise exercise : recommendedExercises) {
            String key = "exercise:" + exercise.getId();
            if (dedup.contains(key)) {
                continue;
            }
            dedup.add(key);
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "exercise");
            recommendation.put("id", exercise.getId());
            recommendation.put("title", crop(exercise.getContent(), 50));
            recommendation.put("reason", "针对练习薄弱环节");
            recommendation.put("priority", "medium");
            recommendations.add(recommendation);
            if (recommendations.size() >= 10) {
                break;
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.addAll(buildDefaultRecommendations());
        }
        return recommendations;
    }

    /**
     * 查找相关课程
     */
    private List<StudyLesson> findRelatedLessons(Long knowledgePointId, String knowledgePointName) {
        List<StudyLesson> allLessons = lessonService.selectStudyLessonList(new StudyLesson());
        if (knowledgePointName == null || knowledgePointName.trim().isEmpty()) {
            return allLessons.stream().limit(2).collect(Collectors.toList());
        }
        String keyword = knowledgePointName.trim();
        List<StudyLesson> filtered = allLessons.stream()
                .filter(lesson -> containsIgnoreCase(lesson.getTitle(), keyword) ||
                        containsIgnoreCase(lesson.getDescription(), keyword) ||
                        containsIgnoreCase(lesson.getTags(), keyword))
                .limit(2)
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            return filtered;
        }
        return allLessons.stream().limit(2).collect(Collectors.toList());
    }

    /**
     * 查找推荐练习
     */
    private List<StudyExercise> findRecommendedExercises(LearningAnalysis analysis) {
        List<StudyExercise> allExercises = exerciseService.selectStudyExerciseList(new StudyExercise());
        Set<Long> weakPointIds = analysis.getWeakPoints().stream()
                .map(item -> item.get("knowledgePointId"))
                .filter(Objects::nonNull)
                .map(item -> (Long) item)
                .collect(Collectors.toSet());

        if (weakPointIds.isEmpty()) {
            return allExercises.stream().limit(5).collect(Collectors.toList());
        }
        List<StudyExercise> filtered = allExercises.stream()
                .filter(exercise -> exercise.getKnowledgePointId() != null && weakPointIds.contains(exercise.getKnowledgePointId()))
                .limit(5)
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            return filtered;
        }
        return allExercises.stream().limit(5).collect(Collectors.toList());
    }

    /**
     * 生成学习路径步骤
     */
    private List<Map<String, Object>> generatePathSteps(List<Map<String, Object>> recommendations, LearningAnalysis analysis) {
        List<Map<String, Object>> steps = new ArrayList<>();

        // 按优先级排序推荐内容
        recommendations.sort((a, b) -> {
            String priorityA = (String) a.get("priority");
            String priorityB = (String) b.get("priority");
            return priorityB.compareTo(priorityA); // 高优先级在前
        });

        // 生成路径步骤
        int stepIndex = 1;
        for (Map<String, Object> recommendation : recommendations) {
            Map<String, Object> step = new HashMap<>();
            step.put("stepIndex", stepIndex++);
            step.put("type", recommendation.get("type"));
            step.put("id", recommendation.get("id"));
            step.put("title", recommendation.get("title"));
            step.put("reason", recommendation.get("reason"));
            step.put("status", "pending");
            steps.add(step);
        }

        return steps;
    }

    private String buildUserProfileContext(Long userId, LearningAnalysis analysis, List<Map<String, Object>> fallbackRecommendations) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户ID: ").append(userId).append("\n");
        sb.append("总练习数: ").append(analysis.getTotalExercises()).append("\n");
        sb.append("正确率: ").append(String.format("%.1f", analysis.getAccuracy())).append("%\n");
        sb.append("学习水平: ").append(analysis.getLearningLevel()).append("\n");

        Object progress = analysis.getProgress() == null ? null : analysis.getProgress().get("progressPercentage");
        double progressValue = progress instanceof Number ? ((Number) progress).doubleValue() : 0D;
        sb.append("学习进度: ").append(String.format("%.1f", progressValue)).append("%\n\n");

        sb.append("薄弱知识点:\n");
        if (analysis.getWeakPoints() == null || analysis.getWeakPoints().isEmpty()) {
            sb.append("- 暂无明显薄弱点\n");
        } else {
            for (Map<String, Object> wp : analysis.getWeakPoints().stream().limit(5).collect(Collectors.toList())) {
                sb.append("- ").append(String.valueOf(wp.get("knowledgePointName")))
                        .append(" (正确率 ").append(String.format("%.1f", toDouble(wp.get("accuracy")))).append("%, ")
                        .append("练习 ").append((Integer) wp.get("totalExercises")).append(" 题)\n");
            }
        }

        List<StudyExerciseRecord> records = exerciseRecordService.selectStudyExerciseRecordByUserId(userId);
        List<StudyExerciseRecord> recentRecords = records.stream().limit(20).collect(Collectors.toList());
        sb.append("\n最近练习记录:\n");
        if (recentRecords.isEmpty()) {
            sb.append("- 暂无练习记录\n");
        } else {
            for (StudyExerciseRecord record : recentRecords) {
                if (record.getExerciseId() == null) {
                    continue;
                }
                StudyExercise exercise = exerciseService.selectStudyExerciseById(record.getExerciseId());
                if (exercise == null) {
                    continue;
                }
                sb.append("- 题目: ").append(crop(exercise.getContent(), 40))
                        .append(" | 是否正确: ").append(Integer.valueOf(1).equals(record.getIsCorrect()) ? "正确" : "错误")
                        .append(" | 知识点ID: ").append(exercise.getKnowledgePointId())
                        .append("\n");
            }
        }

        sb.append("\n可选资源池(供模型选用):\n");
        for (Map<String, Object> rec : fallbackRecommendations.stream().limit(10).collect(Collectors.toList())) {
            sb.append("- ").append(rec.get("type")).append(" | id=").append(rec.get("id"))
                    .append(" | title=").append(rec.get("title"))
                    .append(" | priority=").append(rec.get("priority"))
                    .append(" | reason=").append(rec.get("reason"))
                    .append("\n");
        }
        return sb.toString();
    }

    private LearningPath parseAiLearningPath(String aiText) {
        if (aiText == null || aiText.trim().isEmpty()) {
            return null;
        }
        String json = extractJsonObject(aiText);
        if (json == null) {
            return null;
        }
        JSONObject root = JSON.parseObject(json);
        LearningPath path = new LearningPath();
        path.setEstimatedDays(safeInt(root.getInteger("estimatedDays"), 14));

        JSONArray recArray = root.getJSONArray("recommendations");
        List<Map<String, Object>> recommendations = new ArrayList<>();
        if (recArray != null) {
            for (int i = 0; i < recArray.size(); i++) {
                JSONObject item = recArray.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                Map<String, Object> recommendation = new LinkedHashMap<>();
                recommendation.put("type", safeType(item.getString("type")));
                recommendation.put("id", safeLong(item.getLong("id"), (long) (i + 1)));
                recommendation.put("title", defaultText(item.getString("title"), "学习任务" + (i + 1)));
                recommendation.put("reason", defaultText(item.getString("reason"), "根据你的学习情况推荐"));
                recommendation.put("priority", safePriority(item.getString("priority")));
                recommendations.add(recommendation);
            }
        }
        path.setRecommendations(recommendations);

        JSONArray stepArray = root.getJSONArray("pathSteps");
        List<Map<String, Object>> steps = new ArrayList<>();
        if (stepArray != null) {
            for (int i = 0; i < stepArray.size(); i++) {
                JSONObject item = stepArray.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                Map<String, Object> step = new LinkedHashMap<>();
                step.put("stepIndex", i + 1);
                step.put("type", safeType(item.getString("type")));
                step.put("id", safeLong(item.getLong("id"), (long) (i + 1)));
                step.put("title", defaultText(item.getString("title"), "学习步骤" + (i + 1)));
                step.put("reason", defaultText(item.getString("reason"), "按计划完成该步骤"));
                step.put("status", "pending");
                steps.add(step);
            }
        }
        path.setPathSteps(steps);
        return path;
    }

    private String extractJsonObject(String content) {
        String cleaned = content.trim();
        cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");
        if (start < 0 || end <= start) {
            return null;
        }
        return cleaned.substring(start, end + 1);
    }

    private String safeType(String type) {
        return "exercise".equalsIgnoreCase(type) ? "exercise" : "lesson";
    }

    private String safePriority(String priority) {
        return "high".equalsIgnoreCase(priority) ? "high" : "medium";
    }

    private Integer safeInt(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Long safeLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultText(String text, String defaultValue) {
        return (text == null || text.trim().isEmpty()) ? defaultValue : text.trim();
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0D;
    }

    private String crop(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= maxLen) {
            return cleaned;
        }
        return cleaned.substring(0, maxLen) + "...";
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (source == null || target == null) {
            return false;
        }
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private List<Map<String, Object>> buildDefaultRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        List<StudyLesson> lessons = lessonService.selectStudyLessonList(new StudyLesson());
        for (StudyLesson lesson : lessons.stream().limit(3).collect(Collectors.toList())) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("type", "lesson");
            recommendation.put("id", lesson.getLessonId());
            recommendation.put("title", lesson.getTitle());
            recommendation.put("reason", "建议先巩固基础课程");
            recommendation.put("priority", "high");
            recommendations.add(recommendation);
        }
        return recommendations;
    }
}
