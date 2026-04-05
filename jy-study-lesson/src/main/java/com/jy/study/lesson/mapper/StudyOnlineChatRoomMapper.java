package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatRoom;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 在线聊天室房间 Mapper 接口
 */
public interface StudyOnlineChatRoomMapper {
    /**
     * 查询可用房间列表（带我的角色）
     *
     * @param userId 当前用户ID
     * @return 房间列表
     */
    List<StudyOnlineChatRoom> selectEnabledRoomsWithMyRole(@Param("userId") Long userId);

    /**
     * 按房间编码查询可用房间
     *
     * @param roomCode 房间编码
     * @return 房间信息
     */
    StudyOnlineChatRoom selectEnabledRoomByCode(@Param("roomCode") String roomCode);

    /**
     * 按房间编码查询房间（不区分状态）
     *
     * @param roomCode 房间编码
     * @return 房间信息
     */
    StudyOnlineChatRoom selectRoomByCode(@Param("roomCode") String roomCode);

    /**
     * 新增房间
     *
     * @param room 房间对象
     * @return 影响行数
     */
    int insertStudyOnlineChatRoom(StudyOnlineChatRoom room);

    /**
     * 更新房间群主
     *
     * @param room 房间对象
     * @return 影响行数
     */
    int updateRoomOwner(StudyOnlineChatRoom room);

    /**
     * 关闭房间（软删除）
     *
     * @param roomCode 房间编码
     * @param updateBy 更新人
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int closeRoom(@Param("roomCode") String roomCode,
                  @Param("updateBy") String updateBy,
                  @Param("updateTime") Date updateTime);

    /**
     * 按房间编码物理删除房间
     *
     * @param roomCode 房间编码
     * @return 影响行数
     */
    int deleteRoomByCode(@Param("roomCode") String roomCode);
}
