package com.jy.study.lesson.service.impl;

import java.util.List;
import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyLessonCategoryMapper;
import com.jy.study.lesson.domain.StudyLessonCategory;
import com.jy.study.lesson.service.IStudyLessonCategoryService;
import com.jy.study.common.core.text.Convert;

/**
 * 课程分类Service业务层处理
 * 
 * @author jily
 * @date 2024-03-14
 */
@Service
public class StudyLessonCategoryServiceImpl implements IStudyLessonCategoryService 
{
    @Autowired
    private StudyLessonCategoryMapper studyLessonCategoryMapper;

    /**
     * 查询课程分类
     * 
     * @param categoryId 课程分类主键
     * @return 课程分类
     */
    @Override
    public StudyLessonCategory selectStudyLessonCategoryByCategoryId(Long categoryId)
    {
        return studyLessonCategoryMapper.selectStudyLessonCategoryByCategoryId(categoryId);
    }

    /**
     * 查询课程分类列表
     * 
     * @param studyLessonCategory 课程分类
     * @return 课程分类
     */
    @Override
    public List<StudyLessonCategory> selectStudyLessonCategoryList(StudyLessonCategory studyLessonCategory)
    {
        return studyLessonCategoryMapper.selectStudyLessonCategoryList(studyLessonCategory);
    }

    /**
     * 新增课程分类
     * 
     * @param studyLessonCategory 课程分类
     * @return 结果
     */
    @Override
    public int insertStudyLessonCategory(StudyLessonCategory studyLessonCategory)
    {
        studyLessonCategory.setCreateTime(DateUtils.getNowDate());
        return studyLessonCategoryMapper.insertStudyLessonCategory(studyLessonCategory);
    }

    /**
     * 修改课程分类
     * 
     * @param studyLessonCategory 课程分类
     * @return 结果
     */
    @Override
    public int updateStudyLessonCategory(StudyLessonCategory studyLessonCategory)
    {
        studyLessonCategory.setUpdateTime(DateUtils.getNowDate());
        return studyLessonCategoryMapper.updateStudyLessonCategory(studyLessonCategory);
    }

    /**
     * 批量删除课程分类
     * 
     * @param categoryIds 需要删除的课程分类主键
     * @return 结果
     */
    @Override
    public int deleteStudyLessonCategoryByCategoryIds(String categoryIds)
    {
        return studyLessonCategoryMapper.deleteStudyLessonCategoryByCategoryIds(Convert.toStrArray(categoryIds));
    }

    /**
     * 删除课程分类信息
     * 
     * @param categoryId 课程分类主键
     * @return 结果
     */
    @Override
    public int deleteStudyLessonCategoryByCategoryId(Long categoryId)
    {
        return studyLessonCategoryMapper.deleteStudyLessonCategoryByCategoryId(categoryId);
    }
} 