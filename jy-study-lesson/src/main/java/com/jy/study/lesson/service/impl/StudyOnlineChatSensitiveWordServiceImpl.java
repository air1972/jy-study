package com.jy.study.lesson.service.impl;

import com.jy.study.lesson.domain.StudyOnlineChatSensitiveWord;
import com.jy.study.lesson.mapper.StudyOnlineChatSensitiveWordMapper;
import com.jy.study.lesson.service.IStudyOnlineChatSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 在线聊天室敏感词 Service 实现
 */
@Service
public class StudyOnlineChatSensitiveWordServiceImpl implements IStudyOnlineChatSensitiveWordService {
    @Autowired
    private StudyOnlineChatSensitiveWordMapper studyOnlineChatSensitiveWordMapper;

    @Override
    public List<StudyOnlineChatSensitiveWord> selectEnabledWords() {
        return studyOnlineChatSensitiveWordMapper.selectEnabledWords();
    }
}
