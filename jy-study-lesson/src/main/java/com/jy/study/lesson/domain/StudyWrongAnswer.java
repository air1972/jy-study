package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 错题本对象 study_wrong_answer
 */
@Data
public class StudyWrongAnswer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 错题ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID */
    private Long exerciseId;

    /** 错误次数 */
    private Integer wrongCount;

    /** 最后一次答错时间 */
    private Date lastWrongTime;

}
