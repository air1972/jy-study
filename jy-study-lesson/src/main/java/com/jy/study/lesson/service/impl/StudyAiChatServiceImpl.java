package com.jy.study.lesson.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyAiChatMapper;
import com.jy.study.lesson.domain.StudyAiChat;
import com.jy.study.lesson.service.IStudyAiChatService;

/**
 * AI对话记录Service业务层处理
 */
@Service
public class StudyAiChatServiceImpl implements IStudyAiChatService {
    @Autowired
    private StudyAiChatMapper studyAiChatMapper;

    /**
     * 查询AI对话记录
     */
    @Override
    public StudyAiChat selectStudyAiChatById(Long chatId) {
        return studyAiChatMapper.selectStudyAiChatById(chatId);
    }

    /**
     * 查询AI对话记录列表
     */
    @Override
    public List<StudyAiChat> selectStudyAiChatList(StudyAiChat studyAiChat) {
        return studyAiChatMapper.selectStudyAiChatList(studyAiChat);
    }

    /**
     * 新增AI对话记录
     */
    @Override
    public int insertStudyAiChat(StudyAiChat studyAiChat) {
        return studyAiChatMapper.insertStudyAiChat(studyAiChat);
    }

    /**
     * 修改AI对话记录
     */
    @Override
    public int updateStudyAiChat(StudyAiChat studyAiChat) {
        return studyAiChatMapper.updateStudyAiChat(studyAiChat);
    }

    /**
     * 批量删除AI对话记录
     */
    @Override
    public int deleteStudyAiChatByIds(Long[] chatIds) {
        return studyAiChatMapper.deleteStudyAiChatByIds(chatIds);
    }

    /**
     * 删除AI对话记录信息
     */
    @Override
    public int deleteStudyAiChatById(Long chatId) {
        return studyAiChatMapper.deleteStudyAiChatById(chatId);
    }
} 