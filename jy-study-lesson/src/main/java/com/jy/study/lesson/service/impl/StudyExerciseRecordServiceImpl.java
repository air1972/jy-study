package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyExerciseRecordMapper;
import com.jy.study.lesson.domain.StudyExerciseRecord;
import com.jy.study.lesson.service.IStudyExerciseRecordService;
import java.util.List;

/**
 * 练习记录Service业务层处理
 */
@Service
public class StudyExerciseRecordServiceImpl implements IStudyExerciseRecordService {
    @Autowired
    private StudyExerciseRecordMapper studyExerciseRecordMapper;

    /**
     * 新增练习记录
     */
    @Override
    public int insertStudyExerciseRecord(StudyExerciseRecord studyExerciseRecord) {
        studyExerciseRecord.setCreateTime(DateUtils.getNowDate());
        return studyExerciseRecordMapper.insertStudyExerciseRecord(studyExerciseRecord);
    }

    /**
     * 根据用户ID查询练习记录
     */
    @Override
    public List<StudyExerciseRecord> selectStudyExerciseRecordByUserId(Long userId) {
        return studyExerciseRecordMapper.selectStudyExerciseRecordByUserId(userId);
    }
}
