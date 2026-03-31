package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * 知识点对象 study_knowledge_point
 */
@Data
public class StudyKnowledgePoint extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 知识点ID */
    private Long id;

    /** 知识点名称 */
    private String name;

    /** 知识点描述 */
    private String description;

    /** 内容类型(article/lesson) */
    private String contentType;

    /** 内容ID */
    private Long contentId;

    /** 来源标题 */
    private String sourceTitle;
}
