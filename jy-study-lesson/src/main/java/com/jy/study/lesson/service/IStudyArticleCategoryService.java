package com.jy.study.lesson.service;

import java.util.List;
import com.jy.study.lesson.domain.StudyArticleCategory;

/**
 * 文章分类Service接口
 * 
 * @author jily
 * @date 2024-03-14
 */
public interface IStudyArticleCategoryService 
{
    /**
     * 查询文章分类
     * 
     * @param categoryId 文章分类主键
     * @return 文章分类
     */
    public StudyArticleCategory selectStudyArticleCategoryByCategoryId(Long categoryId);

    /**
     * 查询文章分类列表
     * 
     * @param studyArticleCategory 文章分类
     * @return 文章分类集合
     */
    public List<StudyArticleCategory> selectStudyArticleCategoryList(StudyArticleCategory studyArticleCategory);

    /**
     * 新增文章分类
     * 
     * @param studyArticleCategory 文章分类
     * @return 结果
     */
    public int insertStudyArticleCategory(StudyArticleCategory studyArticleCategory);

    /**
     * 修改文章分类
     * 
     * @param studyArticleCategory 文章分类
     * @return 结果
     */
    public int updateStudyArticleCategory(StudyArticleCategory studyArticleCategory);

    /**
     * 批量删除文章分类
     * 
     * @param categoryIds 需要删除的文章分类主键集合
     * @return 结果
     */
    public int deleteStudyArticleCategoryByCategoryIds(String categoryIds);

    /**
     * 删除文章分类信息
     * 
     * @param categoryId 文章分类主键
     * @return 结果
     */
    public int deleteStudyArticleCategoryByCategoryId(Long categoryId);
} 