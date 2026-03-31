package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 练习记录对象 study_exercise_record
 */
@Data
public class StudyExerciseRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录 ID */
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 题目 ID */
    private Long exerciseId;

    /** 用户答案 */
    private String userAnswer;

    /** 是否正确（0-错误 1-正确） */
    private Integer isCorrect;

    /**
     * 手动定义 getter 方法，避免 Lombok 生成方法名不一致
     */
    public Integer getIsCorrect() {
        return isCorrect;
    }

    /**
     * 手动定义 setter 方法
     */
    public void setIsCorrect(Integer isCorrect) {
        this.isCorrect = isCorrect;
    }
}
