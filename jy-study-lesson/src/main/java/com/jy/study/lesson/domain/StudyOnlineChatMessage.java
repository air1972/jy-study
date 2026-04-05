package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 在线聊天室消息对象 study_online_chat_message
 */
public class StudyOnlineChatMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 房间编码 */
    private String roomCode;

    /** 发送用户ID */
    private Long userId;

    /** 登录账号 */
    private String loginName;

    /** 显示昵称 */
    private String userName;

    /** 回复消息ID */
    private Long replyMessageId;

    /** 被回复用户昵称 */
    private String replyUserName;

    /** 被回复消息摘要 */
    private String replyContent;

    /** 消息内容 */
    private String content;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getReplyMessageId() {
        return replyMessageId;
    }

    public void setReplyMessageId(Long replyMessageId) {
        this.replyMessageId = replyMessageId;
    }

    public String getReplyUserName() {
        return replyUserName;
    }

    public void setReplyUserName(String replyUserName) {
        this.replyUserName = replyUserName;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("messageId", getMessageId())
                .append("roomCode", getRoomCode())
                .append("userId", getUserId())
                .append("loginName", getLoginName())
                .append("userName", getUserName())
                .append("replyMessageId", getReplyMessageId())
                .append("replyUserName", getReplyUserName())
                .append("replyContent", getReplyContent())
                .append("content", getContent())
                .append("createTime", getCreateTime())
                .toString();
    }
}
