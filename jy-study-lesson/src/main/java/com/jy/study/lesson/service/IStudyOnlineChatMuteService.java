package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyOnlineChatMute;

import java.util.Date;

/**
 * 在线聊天室禁言 Service 接口
 */
public interface IStudyOnlineChatMuteService {
    /**
     * 查询当前生效的禁言记录
     *
     * @param userId 用户ID
     * @param roomCode 房间编码
     * @param now 当前时间
     * @return 禁言记录
     */
    StudyOnlineChatMute selectActiveMute(Long userId, String roomCode, Date now);

    /**
     * 新增禁言
     *
     * @param mute 禁言对象
     * @return 影响行数
     */
    int insertStudyOnlineChatMute(StudyOnlineChatMute mute);

    /**
     * 解除房间禁言
     *
     * @param userId 用户ID
     * @param roomCode 房间编码
     * @param updateBy 更新人
     * @return 影响行数
     */
    int disableRoomMute(Long userId, String roomCode, String updateBy);
}
