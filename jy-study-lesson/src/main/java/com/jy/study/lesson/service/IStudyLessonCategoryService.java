package com.jy.study.lesson.service;

import java.util.List;
import com.jy.study.lesson.domain.StudyLessonCategory;

/**
 * 课程分类Service接口
 * 
 * @author jily
 * @date 2024-03-14
 */
public interface IStudyLessonCategoryService 
{
    /**
     * 查询课程分类
     * 
     * @param categoryId 课程分类主键
     * @return 课程分类
     */
    public StudyLessonCategory selectStudyLessonCategoryByCategoryId(Long categoryId);

    /**
     * 查询课程分类列表
     * 
     * @param studyLessonCategory 课程分类
     * @return 课程分类集合
     */
    public List<StudyLessonCategory> selectStudyLessonCategoryList(StudyLessonCategory studyLessonCategory);

    /**
     * 新增课程分类
     * 
     * @param studyLessonCategory 课程分类
     * @return 结果
     */
    public int insertStudyLessonCategory(StudyLessonCategory studyLessonCategory);

    /**
     * 修改课程分类
     * 
     * @param studyLessonCategory 课程分类
     * @return 结果
     */
    public int updateStudyLessonCategory(StudyLessonCategory studyLessonCategory);

    /**
     * 批量删除课程分类
     * 
     * @param categoryIds 需要删除的课程分类主键集合
     * @return 结果
     */
    public int deleteStudyLessonCategoryByCategoryIds(String categoryIds);

    /**
     * 删除课程分类信息
     * 
     * @param categoryId 课程分类主键
     * @return 结果
     */
    public int deleteStudyLessonCategoryByCategoryId(Long categoryId);
} 