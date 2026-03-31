package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyLesson;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyLessonMapper {
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
     * 删除课程
     *
     * @param lessonId 课程主键
     * @return 结果
     */
    public int deleteStudyLessonByLessonId(Long lessonId);

    /**
     * 批量删除课程
     *
     * @param lessonIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStudyLessonByLessonIds(String[] lessonIds);

    // 计数相关方法
    public int incrementViewCount(Long lessonId);
    public int incrementLikeCount(Long lessonId);
    public int decrementLikeCount(Long lessonId);
    public int incrementCollectCount(Long lessonId);
    public int decrementCollectCount(Long lessonId);

    /**
     * 更新课程浏览量
     */
    public int updateViewCount(StudyLesson lesson);

    /**
     * 统计课程数量
     * @param type 统计类型
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