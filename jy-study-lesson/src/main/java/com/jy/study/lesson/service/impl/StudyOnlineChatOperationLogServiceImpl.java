package com.jy.study.lesson.service.impl;

import com.jy.study.lesson.domain.StudyOnlineChatOperationLog;
import com.jy.study.lesson.mapper.StudyOnlineChatOperationLogMapper;
import com.jy.study.lesson.service.IStudyOnlineChatOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 在线聊天室管理操作审计日志 Service 实现
 */
@Service
public class StudyOnlineChatOperationLogServiceImpl implements IStudyOnlineChatOperationLogService {
    @Autowired
    private StudyOnlineChatOperationLogMapper studyOnlineChatOperationLogMapper;

    @Override
    public int insertStudyOnlineChatOperationLog(StudyOnlineChatOperationLog log) {
        return studyOnlineChatOperationLogMapper.insertStudyOnlineChatOperationLog(log);
    }

    @Override
    public List<StudyOnlineChatOperationLog> selectRecentLogs(String roomCode, Integer limitSize) {
        List<StudyOnlineChatOperationLog> logs = studyOnlineChatOperationLogMapper.selectRecentLogs(roomCode, limitSize);
        return logs == null ? Collections.emptyList() : logs;
    }
}
