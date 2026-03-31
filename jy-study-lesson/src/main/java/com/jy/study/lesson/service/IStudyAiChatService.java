package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyAiChat;
import java.util.List;

/**
 * AI对话记录Service接口
 */
public interface IStudyAiChatService {
    /**
     * 查询AI对话记录
     */
    public StudyAiChat selectStudyAiChatById(Long chatId);

    /**
     * 查询AI对话记录列表
     */
    public List<StudyAiChat> selectStudyAiChatList(StudyAiChat studyAiChat);

    /**
     * 新增AI对话记录
     */
    public int insertStudyAiChat(StudyAiChat studyAiChat);

    /**
     * 修改AI对话记录
     */
    public int updateStudyAiChat(StudyAiChat studyAiChat);

    /**
     * 批量删除AI对话记录
     */
    public int deleteStudyAiChatByIds(Long[] chatIds);

    /**
     * 删除AI对话记录信息
     */
    public int deleteStudyAiChatById(Long chatId);
} 