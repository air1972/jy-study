package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatRoomUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 在线聊天室房间成员 Mapper 接口
 */
public interface StudyOnlineChatRoomUserMapper {
    /**
     * 查询成员信息
     *
     * @param roomCode 房间编码
     * @param userId 用户ID
     * @return 成员信息
     */
    StudyOnlineChatRoomUser selectRoomUser(@Param("roomCode") String roomCode, @Param("userId") Long userId);

    /**
     * 新增或激活成员
     *
     * @param roomUser 成员对象
     * @return 影响行数
     */
    int upsertRoomUser(StudyOnlineChatRoomUser roomUser);

    /**
     * 更新成员角色
     *
     * @param roomCode 房间编码
     * @param userId 用户ID
     * @param role 角色
     * @param updateBy 更新人
     * @return 影响行数
     */
    int updateRoomUserRole(@Param("roomCode") String roomCode,
                           @Param("userId") Long userId,
                           @Param("role") String role,
                           @Param("updateBy") String updateBy);

    /**
     * 查询房间内生效成员列表
     *
     * @param roomCode 房间编码
     * @return 成员列表
     */
    List<StudyOnlineChatRoomUser> selectEnabledRoomUsers(@Param("roomCode") String roomCode);

    /**
     * 关闭房间时批量失效成员关系
     *
     * @param roomCode 房间编码
     * @param updateBy 更新人
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int disableRoomUsers(@Param("roomCode") String roomCode,
                         @Param("updateBy") String updateBy,
                         @Param("updateTime") Date updateTime);

    /**
     * 按房间编码物理删除成员关系
     *
     * @param roomCode 房间编码
     * @return 影响行数
     */
    int deleteRoomUsersByRoomCode(@Param("roomCode") String roomCode);
}
