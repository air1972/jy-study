package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatSensitiveWord;

import java.util.List;

/**
 * 在线聊天室敏感词 Mapper 接口
 */
public interface StudyOnlineChatSensitiveWordMapper {
    /**
     * 查询生效中的敏感词列表
     *
     * @return 敏感词列表
     */
    List<StudyOnlineChatSensitiveWord> selectEnabledWords();
}
