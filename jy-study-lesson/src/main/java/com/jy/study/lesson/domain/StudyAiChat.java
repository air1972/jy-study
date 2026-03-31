package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * AI对话记录对象 study_ai_chat
 */
public class StudyAiChat extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 对话ID */
    private Long chatId;

    /** 会话ID */
    private String conversationId;

    /** 用户ID */
    private Long userId;

    /** 角色 */
    private String role;

    /** 消息内容 */
    private String content;

    /** 使用的模型 */
    private String model;

    /** 状态（0正常 1删除） */
    private String status;

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("chatId", getChatId())
                .append("conversationId", getConversationId())
                .append("userId", getUserId())
                .append("role", getRole())
                .append("content", getContent())
                .append("model", getModel())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
} 