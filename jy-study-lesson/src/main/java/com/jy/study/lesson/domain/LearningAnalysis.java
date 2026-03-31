package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 学习分析结果
 */
@Data
public class LearningAnalysis extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 总练习数 */
    private int totalExercises;

    /** 正确率 */
    private double accuracy;

    /** 薄弱知识点 */
    private List<Map<String, Object>> weakPoints;

    /** 学习进度 */
    private Map<String, Object> progress;

    /** 学习能力水平 */
    private String learningLevel;
}
