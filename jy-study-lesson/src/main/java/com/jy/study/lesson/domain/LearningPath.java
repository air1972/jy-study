package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 个性化学习路径
 */
@Data
public class LearningPath extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 路径ID */
    private Long pathId;

    /** 用户ID */
    private Long userId;

    /** 推荐内容 */
    private List<Map<String, Object>> recommendations;

    /** 学习路径步骤 */
    private List<Map<String, Object>> pathSteps;

    /** 路径状态 */
    private String status; // active, completed, expired

    /** 预计完成时间（天） */
    private Integer estimatedDays;
}
