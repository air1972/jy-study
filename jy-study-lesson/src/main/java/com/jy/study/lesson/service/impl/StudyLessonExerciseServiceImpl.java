package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyLessonExercise;
import com.jy.study.lesson.mapper.StudyLessonExerciseMapper;
import com.jy.study.lesson.service.IStudyLessonExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 课程练习 Service 业务层处理
 *
 * @author jily
 * @date 2026-03-26
 */
@Service
public class StudyLessonExerciseServiceImpl implements IStudyLessonExerciseService
{
    @Autowired
    private StudyLessonExerciseMapper studyLessonExerciseMapper;

    /**
     * 查询课程练习
     *
     * @param exerciseId 课程练习 ID
     * @return 课程练习
     */
    @Override
    public StudyLessonExercise selectStudyLessonExerciseById(Long exerciseId)
    {
        return studyLessonExerciseMapper.selectStudyLessonExerciseById(exerciseId);
    }

    /**
     * 根据课程 ID 查询练习列表
     *
     * @param lessonId 课程 ID
     * @return 课程练习
     */
    @Override
    public List<StudyLessonExercise> selectStudyLessonExerciseByLessonId(Long lessonId)
    {
        return studyLessonExerciseMapper.selectStudyLessonExerciseByLessonId(lessonId);
    }

    /**
     * 查询课程练习列表
     *
     * @param studyLessonExercise 课程练习
     * @return 课程练习
     */
    @Override
    public List<StudyLessonExercise> selectStudyLessonExerciseList(StudyLessonExercise studyLessonExercise)
    {
        return studyLessonExerciseMapper.selectStudyLessonExerciseList(studyLessonExercise);
    }

    /**
     * 新增课程练习
     *
     * @param studyLessonExercise 课程练习
     * @return 结果
     */
    @Override
    public int insertStudyLessonExercise(StudyLessonExercise studyLessonExercise)
    {
        studyLessonExercise.setCreateTime(DateUtils.getNowDate());
        return studyLessonExerciseMapper.insertStudyLessonExercise(studyLessonExercise);
    }

    /**
     * 修改课程练习
     *
     * @param studyLessonExercise 课程练习
     * @return 结果
     */
    @Override
    public int updateStudyLessonExercise(StudyLessonExercise studyLessonExercise)
    {
        studyLessonExercise.setUpdateTime(DateUtils.getNowDate());
        return studyLessonExerciseMapper.updateStudyLessonExercise(studyLessonExercise);
    }

    /**
     * 批量删除课程练习
     *
     * @param exerciseIds 需要删除的课程练习 ID
     * @return 结果
     */
    @Override
    public int deleteStudyLessonExerciseByIds(String exerciseIds)
    {
        return studyLessonExerciseMapper.deleteStudyLessonExerciseByIds(exerciseIds.split(","));
    }

    /**
     * 删除课程练习信息
     *
     * @param exerciseId 课程练习 ID
     * @return 结果
     */
    @Override
    public int deleteStudyLessonExerciseById(Long exerciseId)
    {
        return studyLessonExerciseMapper.deleteStudyLessonExerciseById(exerciseId);
    }
}
