package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 在线聊天室房间对象 study_online_chat_room
 */
public class StudyOnlineChatRoom extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 房间ID */
    private Long roomId;

    /** 房间编码 */
    private String roomCode;

    /** 房间名称 */
    private String roomName;

    /** 群主用户ID */
    private Long ownerUserId;

    /** 群主登录账号 */
    private String ownerLoginName;

    /** 群主显示名 */
    private String ownerUserName;

    /** 状态（0正常 1关闭） */
    private String status;

    /** 当前用户在该房间中的角色（owner/admin/member） */
    private String myRole;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerLoginName() {
        return ownerLoginName;
    }

    public void setOwnerLoginName(String ownerLoginName) {
        this.ownerLoginName = ownerLoginName;
    }

    public String getOwnerUserName() {
        return ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("roomId", getRoomId())
                .append("roomCode", getRoomCode())
                .append("roomName", getRoomName())
                .append("ownerUserId", getOwnerUserId())
                .append("ownerLoginName", getOwnerLoginName())
                .append("ownerUserName", getOwnerUserName())
                .append("status", getStatus())
                .append("myRole", getMyRole())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
