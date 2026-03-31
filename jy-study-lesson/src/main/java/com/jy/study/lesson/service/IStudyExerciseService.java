package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyExercise;
import java.util.List;

/**
 * 题目Service接口
 */
public interface IStudyExerciseService {
    /**
     * 根据ID查询题目
     */
    public StudyExercise selectStudyExerciseById(Long id);

    /**
     * 查询题目列表
     */
    public List<StudyExercise> selectStudyExerciseList(StudyExercise studyExercise);

    /**
     * 新增题目
     */
    public int insertStudyExercise(StudyExercise studyExercise);

    /**
     * 修改题目
     */
    public int updateStudyExercise(StudyExercise studyExercise);

    /**
     * 删除题目
     */
    public int deleteStudyExerciseById(Long id);

    /**
     * 批量删除题目
     */
    public int deleteStudyExerciseByIds(String ids);
}
