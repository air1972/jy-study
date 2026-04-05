package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 在线聊天室管理操作审计日志对象 study_online_chat_operation_log
 */
public class StudyOnlineChatOperationLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 房间编码 */
    private String roomCode;

    /** 操作类型 */
    private String actionType;

    /** 操作人用户ID */
    private Long operatorUserId;

    /** 操作人登录账号 */
    private String operatorLoginName;

    /** 操作人昵称 */
    private String operatorUserName;

    /** 目标用户ID */
    private Long targetUserId;

    /** 目标用户昵称 */
    private String targetUserName;

    /** 详情 */
    private String detail;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getOperatorLoginName() {
        return operatorLoginName;
    }

    public void setOperatorLoginName(String operatorLoginName) {
        this.operatorLoginName = operatorLoginName;
    }

    public String getOperatorUserName() {
        return operatorUserName;
    }

    public void setOperatorUserName(String operatorUserName) {
        this.operatorUserName = operatorUserName;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("logId", getLogId())
                .append("roomCode", getRoomCode())
                .append("actionType", getActionType())
                .append("operatorUserId", getOperatorUserId())
                .append("operatorLoginName", getOperatorLoginName())
                .append("operatorUserName", getOperatorUserName())
                .append("targetUserId", getTargetUserId())
                .append("targetUserName", getTargetUserName())
                .append("detail", getDetail())
                .append("createTime", getCreateTime())
                .toString();
    }
}
