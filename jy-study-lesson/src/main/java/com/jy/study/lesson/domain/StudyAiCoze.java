package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * AI试题生成记录对象 study_ai_coze
 */
public class StudyAiCoze extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 关联文章ID */
    private Long articleId;

    /** 题目总数 */
    private Integer questionNum;

    /** 题目内容 */
    private String content;

    /** 文件URL */
    private String fileUrl;

    /** 调试URL */
    private String debugUrl;

    /** 状态（0生成中 1已完成 2失败） */
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Integer getQuestionNum() {
        return questionNum;
    }

    public void setQuestionNum(Integer questionNum) {
        this.questionNum = questionNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDebugUrl() {
        return debugUrl;
    }

    public void setDebugUrl(String debugUrl) {
        this.debugUrl = debugUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("articleId", getArticleId())
                .append("questionNum", getQuestionNum())
                .append("content", getContent())
                .append("fileUrl", getFileUrl())
                .append("debugUrl", getDebugUrl())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
} 