package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyOnlineChatRoom;
import com.jy.study.lesson.domain.StudyOnlineChatRoomUser;

import java.util.List;

/**
 * 在线聊天室房间 Service 接口
 */
public interface IStudyOnlineChatRoomService {
    /**
     * 确保房间存在且当前用户已加入房间
     *
     * @param roomCode 房间编码
     * @param userId 用户ID
     * @param loginName 登录账号
     * @param userName 显示昵称
     * @return 房间对象
     */
    StudyOnlineChatRoom ensureRoomAndJoin(String roomCode, Long userId, String loginName, String userName);

    /**
     * 查询全部可用房间（附带当前用户角色）
     *
     * @param userId 用户ID
     * @return 房间列表
     */
    List<StudyOnlineChatRoom> selectEnabledRoomsWithMyRole(Long userId);

    /**
     * 查询可用房间
     *
     * @param roomCode 房间编码
     * @return 房间对象
     */
    StudyOnlineChatRoom selectEnabledRoomByCode(String roomCode);

    /**
     * 查询房间成员
     *
     * @param roomCode 房间编码
     * @return 成员列表
     */
    List<StudyOnlineChatRoomUser> selectEnabledRoomUsers(String roomCode);

    /**
     * 查询我在房间中的角色
     *
     * @param roomCode 房间编码
     * @param userId 用户ID
     * @return 角色（owner/admin/member/visitor）
     */
    String selectMyRole(String roomCode, Long userId);

    /**
     * 创建房间
     *
     * @param roomCode 房间编码
     * @param roomName 房间名称
     * @param userId 创建人ID
     * @param loginName 创建人登录名
     * @param userName 创建人昵称
     * @param operator 操作人标识
     * @return 房间对象
     */
    StudyOnlineChatRoom createRoom(String roomCode, String roomName, Long userId, String loginName, String userName, String operator);

    /**
     * 设置或取消管理员
     *
     * @param roomCode 房间编码
     * @param operatorUserId 操作人ID
     * @param targetUserId 目标用户ID
     * @param admin true设置管理员 false取消管理员
     * @param updateBy 更新人
     */
    void setAdmin(String roomCode, Long operatorUserId, Long targetUserId, boolean admin, String updateBy);

    /**
     * 转让群主
     *
     * @param roomCode 房间编码
     * @param operatorUserId 当前群主ID
     * @param targetUserId 目标用户ID
     * @param updateBy 更新人
     */
    void transferOwner(String roomCode, Long operatorUserId, Long targetUserId, String updateBy);

    /**
     * 删除群聊（物理删除房间与关联数据）
     *
     * @param roomCode 房间编码
     * @param operatorUserId 操作人ID（必须群主）
     * @param updateBy 更新人
     */
    void deleteRoom(String roomCode, Long operatorUserId, String updateBy);

    /**
     * 判断是否具备房间管理权限（owner/admin）
     *
     * @param roomCode 房间编码
     * @param userId 用户ID
     * @return true 有权限
     */
    boolean canManageRoom(String roomCode, Long userId);
}
