package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatMute;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 在线聊天室禁言 Mapper 接口
 */
public interface StudyOnlineChatMuteMapper {
    /**
     * 查询当前生效的禁言记录
     *
     * @param userId 用户ID
     * @param roomCode 房间编码
     * @param now 当前时间
     * @return 禁言记录
     */
    StudyOnlineChatMute selectActiveMute(@Param("userId") Long userId,
                                         @Param("roomCode") String roomCode,
                                         @Param("now") Date now);

    /**
     * 新增禁言记录
     *
     * @param mute 禁言对象
     * @return 影响行数
     */
    int insertStudyOnlineChatMute(StudyOnlineChatMute mute);

    /**
     * 解除房间内生效禁言
     *
     * @param userId 用户ID
     * @param roomCode 房间编码
     * @param updateBy 更新人
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int disableRoomMute(@Param("userId") Long userId,
                        @Param("roomCode") String roomCode,
                        @Param("updateBy") String updateBy,
                        @Param("updateTime") Date updateTime);

    /**
     * 按房间编码删除禁言记录
     *
     * @param roomCode 房间编码
     * @return 影响行数
     */
    int deleteMutesByRoomCode(@Param("roomCode") String roomCode);
}
