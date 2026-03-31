package com.jy.study.lesson.service.impl;

import java.util.List;
import java.util.Map;
import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.mapper.StudyLessonMapper;
import com.jy.study.lesson.service.IStudyLessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jy.study.common.core.text.Convert;

/**
 * 课程Service业务层处理
 * 
 * @author jily
 * @date 2025-01-14
 */
@Service
public class StudyLessonServiceImpl implements IStudyLessonService
{
    @Autowired
    private StudyLessonMapper studyLessonMapper;

    /**
     * 查询课程
     * 
     * @param lessonId 课程主键
     * @return 课程
     */
    @Override
    public StudyLesson selectStudyLessonByLessonId(Long lessonId)
    {
        return studyLessonMapper.selectStudyLessonByLessonId(lessonId);
    }

    /**
     * 查询课程列表
     * 
     * @param studyLesson 课程
     * @return 课程
     */
    @Override
    public List<StudyLesson> selectStudyLessonList(StudyLesson studyLesson)
    {
        return studyLessonMapper.selectStudyLessonList(studyLesson);
    }

    /**
     * 新增课程
     * 
     * @param studyLesson 课程
     * @return 结果
     */
    @Override
    public int insertStudyLesson(StudyLesson studyLesson)
    {
        studyLesson.setCreateTime(DateUtils.getNowDate());
        return studyLessonMapper.insertStudyLesson(studyLesson);
    }

    /**
     * 修改课程
     * 
     * @param studyLesson 课程
     * @return 结果
     */
    @Override
    public int updateStudyLesson(StudyLesson studyLesson)
    {
        studyLesson.setUpdateTime(DateUtils.getNowDate());
        return studyLessonMapper.updateStudyLesson(studyLesson);
    }

    /**
     * 批量删除课程
     * 
     * @param lessonIds 需要删除的课程主键
     * @return 结果
     */
    @Override
    public int deleteStudyLessonByLessonIds(String lessonIds)
    {
        return studyLessonMapper.deleteStudyLessonByLessonIds(Convert.toStrArray(lessonIds));
    }

    /**
     * 删除课程信息
     * 
     * @param lessonId 课程主键
     * @return 结果
     */
    @Override
    public int deleteStudyLessonByLessonId(Long lessonId)
    {
        return studyLessonMapper.deleteStudyLessonByLessonId(lessonId);
    }

    /**
     * 更新课程浏览量
     */
    @Override
    public int updateViewCount(Long lessonId) {
        StudyLesson lesson = new StudyLesson();
        lesson.setLessonId(lessonId);
        lesson.setViewCount(1L); // 每次增加1
        return studyLessonMapper.updateViewCount(lesson);
    }

    /**
     * 统计课程数量
     * @param type 统计类型:
     *            null-总数
     *            month-本月新增(根据create_time)
     * @return 课程数量
     */
    @Override
    public Long selectLessonCount(String type) {
        return studyLessonMapper.selectLessonCount(type);
    }

    /**
     * 获取课程增长趋势
     * @return 最近6个月的课程数量统计
     */
    @Override
    public List<Map<String, Object>> selectLessonTrend() {
        return studyLessonMapper.selectLessonTrend();
    }

    /**
     * 统计课程分类数据
     * @return 分类及其数量
     */
    @Override
    public List<Map<String, Object>> selectCategoryStats() {
        return studyLessonMapper.selectCategoryStats();
    }

    /**
     * 获取热门课程
     * @param limit 获取数量
     * @return 课程列表
     */
    @Override
    public List<Map<String, Object>> selectTopLessons(int limit) {
        return studyLessonMapper.selectTopLessons(limit);
    }
}
