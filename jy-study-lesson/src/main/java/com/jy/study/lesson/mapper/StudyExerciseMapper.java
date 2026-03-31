package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyExercise;
import java.util.List;

/**
 * 题目Mapper接口
 */
public interface StudyExerciseMapper {
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
    public int deleteStudyExerciseByIds(String[] ids);
}
