package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class StudyComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long commentId;

    private Long userId;

    /**
     * 评论类型：1-课程，2-文章
     */
    private String type;

    private Long targetId;

    private String content;

    /**
     * 状态：0-正常，1-隐藏
     */
    private String status;

    /**
     * 以下字段为查询展示扩展字段
     */
    private String userName;

    private String avatar;

    private String createTimeText;

    private String targetTitle;

    private String typeName;
}
