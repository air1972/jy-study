package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyLessonExercise;
import java.util.List;

/**
 * 课程练习 Mapper 接口
 *
 * @author jily
 * @date 2026-03-26
 */
public interface StudyLessonExerciseMapper
{
    /**
     * 查询课程练习
     *
     * @param exerciseId 课程练习 ID
     * @return 课程练习
     */
    public StudyLessonExercise selectStudyLessonExerciseById(Long exerciseId);

    /**
     * 根据课程 ID 查询练习列表
     *
     * @param lessonId 课程 ID
     * @return 课程练习集合
     */
    public List<StudyLessonExercise> selectStudyLessonExerciseByLessonId(Long lessonId);

    /**
     * 查询课程练习列表
     *
     * @param studyLessonExercise 课程练习
     * @return 课程练习集合
     */
    public List<StudyLessonExercise> selectStudyLessonExerciseList(StudyLessonExercise studyLessonExercise);

    /**
     * 新增课程练习
     *
     * @param studyLessonExercise 课程练习
     * @return 结果
     */
    public int insertStudyLessonExercise(StudyLessonExercise studyLessonExercise);

    /**
     * 修改课程练习
     *
     * @param studyLessonExercise 课程练习
     * @return 结果
     */
    public int updateStudyLessonExercise(StudyLessonExercise studyLessonExercise);

    /**
     * 删除课程练习
     *
     * @param exerciseId 课程练习 ID
     * @return 结果
     */
    public int deleteStudyLessonExerciseById(Long exerciseId);

    /**
     * 批量删除课程练习
     *
     * @param exerciseIds 需要删除的数据 ID
     * @return 结果
     */
    public int deleteStudyLessonExerciseByIds(String[] exerciseIds);
}
