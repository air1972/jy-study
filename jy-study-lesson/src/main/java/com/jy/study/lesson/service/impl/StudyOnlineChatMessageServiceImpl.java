package com.jy.study.lesson.service.impl;

import com.jy.study.lesson.domain.StudyOnlineChatMessage;
import com.jy.study.lesson.mapper.StudyOnlineChatMessageMapper;
import com.jy.study.lesson.service.IStudyOnlineChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在线聊天室消息 Service 业务层处理
 */
@Service
public class StudyOnlineChatMessageServiceImpl implements IStudyOnlineChatMessageService {
    @Autowired
    private StudyOnlineChatMessageMapper studyOnlineChatMessageMapper;

    @Override
    public List<StudyOnlineChatMessage> selectRecentMessages(String roomCode, Integer limitSize) {
        return studyOnlineChatMessageMapper.selectRecentMessages(roomCode, limitSize);
    }

    @Override
    public List<StudyOnlineChatMessage> selectMessagesAfterId(String roomCode, Long lastMessageId, Integer limitSize) {
        return studyOnlineChatMessageMapper.selectMessagesAfterId(roomCode, lastMessageId, limitSize);
    }

    @Override
    public StudyOnlineChatMessage selectMessageByIdInRoom(String roomCode, Long messageId) {
        return studyOnlineChatMessageMapper.selectMessageByIdInRoom(roomCode, messageId);
    }

    @Override
    public int insertStudyOnlineChatMessage(StudyOnlineChatMessage message) {
        return studyOnlineChatMessageMapper.insertStudyOnlineChatMessage(message);
    }
}
