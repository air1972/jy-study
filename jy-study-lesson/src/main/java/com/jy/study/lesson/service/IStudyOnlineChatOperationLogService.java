package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyOnlineChatOperationLog;

import java.util.List;

/**
 * 在线聊天室管理操作审计日志 Service 接口
 */
public interface IStudyOnlineChatOperationLogService {
    /**
     * 新增操作日志
     *
     * @param log 日志对象
     * @return 影响行数
     */
    int insertStudyOnlineChatOperationLog(StudyOnlineChatOperationLog log);

    /**
     * 查询房间最近操作日志
     *
     * @param roomCode 房间编码
     * @param limitSize 条数
     * @return 日志列表
     */
    List<StudyOnlineChatOperationLog> selectRecentLogs(String roomCode, Integer limitSize);
}
