package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyLesson;

import java.util.List;
import java.util.Map;


/**
 * 课程Service接口
 * 
 * @author jily
 * @date 2025-01-14
 */
public interface IStudyLessonService 
{
    /**
     * 查询课程
     * 
     * @param lessonId 课程主键
     * @return 课程
     */
    public StudyLesson selectStudyLessonByLessonId(Long lessonId);

    /**
     * 查询课程列表
     * 
     * @param studyLesson 课程
     * @return 课程集合
     */
    public List<StudyLesson> selectStudyLessonList(StudyLesson studyLesson);

    /**
     * 新增课程
     * 
     * @param studyLesson 课程
     * @return 结果
     */
    public int insertStudyLesson(StudyLesson studyLesson);

    /**
     * 修改课程
     * 
     * @param studyLesson 课程
     * @return 结果
     */
    public int updateStudyLesson(StudyLesson studyLesson);

    /**
     * 批量删除课程
     * 
     * @param lessonIds 需要删除的课程主键集合
     * @return 结果
     */
    public int deleteStudyLessonByLessonIds(String lessonIds);

    /**
     * 删除课程信息
     * 
     * @param lessonId 课程主键
     * @return 结果
     */
    public int deleteStudyLessonByLessonId(Long lessonId);

    /**
     * 更新课程浏览量
     */
    public int updateViewCount(Long lessonId);

    /**
     * 统计课程数量
     * @param type 统计类型:
     *            null-总数
     *            month-本月新增(根据create_time)
     * @return 课程数量
     */
    public Long selectLessonCount(String type);

    /**
     * 获取课程增长趋势
     * @return 最近6个月的课程数量统计
     */
    public List<Map<String, Object>> selectLessonTrend();

    /**
     * 统计课程分类数据
     * @return 分类及其数量
     */
    public List<Map<String, Object>> selectCategoryStats();

    /**
     * 获取热门课程
     * @param limit 获取数量
     * @return 课程列表
     */
    public List<Map<String, Object>> selectTopLessons(int limit);
}
