package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyOnlineChatOperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 在线聊天室管理操作审计日志 Mapper 接口
 */
public interface StudyOnlineChatOperationLogMapper {
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
    List<StudyOnlineChatOperationLog> selectRecentLogs(@Param("roomCode") String roomCode,
                                                       @Param("limitSize") Integer limitSize);

    /**
     * 按房间编码删除操作日志
     *
     * @param roomCode 房间编码
     * @return 影响行数
     */
    int deleteOperationLogsByRoomCode(@Param("roomCode") String roomCode);
}
