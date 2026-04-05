package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyOnlineChatMessage;

import java.util.List;

/**
 * 在线聊天室消息 Service 接口
 */
public interface IStudyOnlineChatMessageService {
    /**
     * 查询最近的聊天室消息
     *
     * @param roomCode 房间编码
     * @param limitSize 返回条数
     * @return 消息列表
     */
    List<StudyOnlineChatMessage> selectRecentMessages(String roomCode, Integer limitSize);

    /**
     * 查询某条消息之后的增量消息
     *
     * @param roomCode 房间编码
     * @param lastMessageId 客户端最后一条消息ID
     * @param limitSize 返回条数
     * @return 消息列表
     */
    List<StudyOnlineChatMessage> selectMessagesAfterId(String roomCode, Long lastMessageId, Integer limitSize);

    /**
     * 查询房间内指定消息
     *
     * @param roomCode 房间编码
     * @param messageId 消息ID
     * @return 消息对象
     */
    StudyOnlineChatMessage selectMessageByIdInRoom(String roomCode, Long messageId);

    /**
     * 新增聊天室消息
     *
     * @param message 消息对象
     * @return 影响行数
     */
    int insertStudyOnlineChatMessage(StudyOnlineChatMessage message);
}
