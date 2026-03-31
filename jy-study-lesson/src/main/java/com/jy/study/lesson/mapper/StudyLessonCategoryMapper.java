package com.jy.study.lesson.mapper;

import java.util.List;
import com.jy.study.lesson.domain.StudyLessonCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程分类Mapper接口
 * 
 * @author jily
 * @date 2024-03-14
 */
@Mapper
public interface StudyLessonCategoryMapper 
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
     * 删除课程分类
     * 
     * @param categoryId 课程分类主键
     * @return 结果
     */
    public int deleteStudyLessonCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除课程分类
     * 
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteStudyLessonCategoryByCategoryIds(String[] categoryIds);
} 