package com.jy.study.lesson.domain.dto;

import java.util.Date;

/**
 * 用户浏览历史DTO
 */
public class UserViewHistoryDTO {
    
    /** 记录ID */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 目标ID（课程ID或文章ID） */
    private Long targetId;
    
    /** 类型（1：课程，2：文章） */
    private String type;
    
    /** 标题 */
    private String title;
    
    /** 文章内容摘要 */
    private String contentSummary;
    
    /** 课程描述摘要 */
    private String descriptionSummary;
    
    /** IP地址 */
    private String ipAddr;
    
    /** 创建时间 */
    private Date createTime;

    public Long getViewId() {
        return id;
    }

    public void setViewId(Long viewId) {
        this.id = viewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public void setContentSummary(String contentSummary) {
        this.contentSummary = contentSummary;
    }

    public String getDescriptionSummary() {
        return descriptionSummary;
    }

    public void setDescriptionSummary(String descriptionSummary) {
        this.descriptionSummary = descriptionSummary;
    }
} 