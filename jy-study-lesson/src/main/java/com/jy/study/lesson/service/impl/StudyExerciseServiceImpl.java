package com.jy.study.lesson.service.impl;

import java.util.List;
import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyExerciseMapper;
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.service.IStudyExerciseService;
import com.jy.study.common.core.text.Convert;

/**
 * 题目Service业务层处理
 */
@Service
public class StudyExerciseServiceImpl implements IStudyExerciseService {
    @Autowired
    private StudyExerciseMapper studyExerciseMapper;

    /**
     * 根据ID查询题目
     */
    @Override
    public StudyExercise selectStudyExerciseById(Long id) {
        return studyExerciseMapper.selectStudyExerciseById(id);
    }

    /**
     * 查询题目列表
     */
    @Override
    public List<StudyExercise> selectStudyExerciseList(StudyExercise studyExercise) {
        return studyExerciseMapper.selectStudyExerciseList(studyExercise);
    }

    /**
     * 新增题目
     */
    @Override
    public int insertStudyExercise(StudyExercise studyExercise) {
        studyExercise.setCreateTime(DateUtils.getNowDate());
        return studyExerciseMapper.insertStudyExercise(studyExercise);
    }

    /**
     * 修改题目
     */
    @Override
    public int updateStudyExercise(StudyExercise studyExercise) {
        studyExercise.setUpdateTime(DateUtils.getNowDate());
        return studyExerciseMapper.updateStudyExercise(studyExercise);
    }

    /**
     * 删除题目
     */
    @Override
    public int deleteStudyExerciseById(Long id) {
        return studyExerciseMapper.deleteStudyExerciseById(id);
    }

    /**
     * 批量删除题目
     */
    @Override
    public int deleteStudyExerciseByIds(String ids) {
        return studyExerciseMapper.deleteStudyExerciseByIds(Convert.toStrArray(ids));
    }
}
