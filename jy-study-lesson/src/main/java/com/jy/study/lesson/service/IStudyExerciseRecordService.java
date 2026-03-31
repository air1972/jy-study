package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyExerciseRecord;
import java.util.List;

/**
 * 练习记录Service接口
 */
public interface IStudyExerciseRecordService {
    /**
     * 新增练习记录
     */
    public int insertStudyExerciseRecord(StudyExerciseRecord studyExerciseRecord);

    /**
     * 根据用户ID查询练习记录
     */
    public List<StudyExerciseRecord> selectStudyExerciseRecordByUserId(Long userId);
}
