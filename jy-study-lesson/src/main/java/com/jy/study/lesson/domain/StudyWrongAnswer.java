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

    /** 来源类型(article/lesson) */
    private String sourceType;

    /** 来源ID */
    private Long sourceId;

    /** 来源标题 */
    private String sourceTitle;

    /** 知识点ID */
    private Long knowledgePointId;

    /** 知识点名称 */
    private String knowledgePointName;

    /** 题目内容 */
    private String content;

    /** 题目类型 */
    private String type;

    /** 正确答案 */
    private String answer;

    /** 解析 */
    private String explanation;

}
