package com.jy.study.lesson.service.impl;

import java.util.List;
import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyArticleCategoryMapper;
import com.jy.study.lesson.domain.StudyArticleCategory;
import com.jy.study.lesson.service.IStudyArticleCategoryService;
import com.jy.study.common.core.text.Convert;

/**
 * 文章分类Service业务层处理
 * 
 * @author jily
 * @date 2024-03-14
 */
@Service
public class StudyArticleCategoryServiceImpl implements IStudyArticleCategoryService 
{
    @Autowired
    private StudyArticleCategoryMapper studyArticleCategoryMapper;

    /**
     * 查询文章分类
     * 
     * @param categoryId 文章分类主键
     * @return 文章分类
     */
    @Override
    public StudyArticleCategory selectStudyArticleCategoryByCategoryId(Long categoryId)
    {
        return studyArticleCategoryMapper.selectStudyArticleCategoryByCategoryId(categoryId);
    }

    /**
     * 查询文章分类列表
     * 
     * @param studyArticleCategory 文章分类
     * @return 文章分类
     */
    @Override
    public List<StudyArticleCategory> selectStudyArticleCategoryList(StudyArticleCategory studyArticleCategory)
    {
        return studyArticleCategoryMapper.selectStudyArticleCategoryList(studyArticleCategory);
    }

    /**
     * 新增文章分类
     * 
     * @param studyArticleCategory 文章分类
     * @return 结果
     */
    @Override
    public int insertStudyArticleCategory(StudyArticleCategory studyArticleCategory)
    {
        studyArticleCategory.setCreateTime(DateUtils.getNowDate());
        return studyArticleCategoryMapper.insertStudyArticleCategory(studyArticleCategory);
    }

    /**
     * 修改文章分类
     * 
     * @param studyArticleCategory 文章分类
     * @return 结果
     */
    @Override
    public int updateStudyArticleCategory(StudyArticleCategory studyArticleCategory)
    {
        studyArticleCategory.setUpdateTime(DateUtils.getNowDate());
        return studyArticleCategoryMapper.updateStudyArticleCategory(studyArticleCategory);
    }

    /**
     * 批量删除文章分类
     * 
     * @param categoryIds 需要删除的文章分类主键
     * @return 结果
     */
    @Override
    public int deleteStudyArticleCategoryByCategoryIds(String categoryIds)
    {
        return studyArticleCategoryMapper.deleteStudyArticleCategoryByCategoryIds(Convert.toStrArray(categoryIds));
    }

    /**
     * 删除文章分类信息
     * 
     * @param categoryId 文章分类主键
     * @return 结果
     */
    @Override
    public int deleteStudyArticleCategoryByCategoryId(Long categoryId)
    {
        return studyArticleCategoryMapper.deleteStudyArticleCategoryByCategoryId(categoryId);
    }
} 