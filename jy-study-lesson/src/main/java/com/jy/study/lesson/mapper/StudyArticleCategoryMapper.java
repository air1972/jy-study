package com.jy.study.lesson.mapper;

import java.util.List;
import com.jy.study.lesson.domain.StudyArticleCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudyArticleCategoryMapper 
{
    public StudyArticleCategory selectStudyArticleCategoryByCategoryId(Long categoryId);

    public List<StudyArticleCategory> selectStudyArticleCategoryList(StudyArticleCategory studyArticleCategory);

    public int insertStudyArticleCategory(StudyArticleCategory studyArticleCategory);

    public int updateStudyArticleCategory(StudyArticleCategory studyArticleCategory);

    public int deleteStudyArticleCategoryByCategoryId(Long categoryId);

    public int deleteStudyArticleCategoryByCategoryIds(String[] categoryIds);
} 