package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 在线聊天室消息 Mapper 接口
 */
public interface StudyOnlineChatMessageMapper {
    /**
     * 查询最近的聊天室消息
     *
     * @param roomCode 房间编码
     * @param limitSize 返回条数
     * @return 消息列表
     */
    List<StudyOnlineChatMessage> selectRecentMessages(@Param("roomCode") String roomCode,
                                                      @Param("limitSize") Integer limitSize);

    /**
     * 查询某条消息之后的增量消息
     *
     * @param roomCode 房间编码
     * @param lastMessageId 客户端最后一条消息ID
     * @param limitSize 返回条数
     * @return 消息列表
     */
    List<StudyOnlineChatMessage> selectMessagesAfterId(@Param("roomCode") String roomCode,
                                                       @Param("lastMessageId") Long lastMessageId,
                                                       @Param("limitSize") Integer limitSize);

    /**
     * 查询房间内指定消息
     *
     * @param roomCode 房间编码
     * @param messageId 消息ID
     * @return 消息对象
     */
    StudyOnlineChatMessage selectMessageByIdInRoom(@Param("roomCode") String roomCode,
                                                   @Param("messageId") Long messageId);

    /**
     * 新增聊天室消息
     *
     * @param message 消息对象
     * @return 影响行数
     */
    int insertStudyOnlineChatMessage(StudyOnlineChatMessage message);

    /**
     * 按房间编码删除聊天消息
     *
     * @param roomCode 房间编码
     * @return 影响行数
     */
    int deleteMessagesByRoomCode(@Param("roomCode") String roomCode);
}
