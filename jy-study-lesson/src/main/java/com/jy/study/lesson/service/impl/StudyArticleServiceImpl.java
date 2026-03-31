package com.jy.study.lesson.service.impl;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.mapper.StudyArticleMapper;
import com.jy.study.lesson.service.IStudyArticleService;

import com.jy.study.common.core.text.Convert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StudyArticleServiceImpl implements IStudyArticleService {
    @Autowired
    private StudyArticleMapper articleMapper;

    @Override
    public StudyArticle selectArticleById(Long articleId) {
        return articleMapper.selectArticleById(articleId);
    }

    @Override
    public List<StudyArticle> selectArticleList(StudyArticle article) {
        return articleMapper.selectArticleList(article);
    }

    @Override
    public int insertArticle(StudyArticle article) {
        return articleMapper.insertArticle(article);
    }

    @Override
    public int updateArticle(StudyArticle article) {
        return articleMapper.updateArticle(article);
    }

    @Override
    public int deleteArticleById(Long articleId) {
        return articleMapper.deleteArticleById(articleId);
    }

    @Override
    public int deleteArticleByIds(String ids) {
        return articleMapper.deleteArticleByIds(Convert.toStrArray(ids));
    }

    @Override
    public Long selectArticleCount(String type) {
        return articleMapper.selectArticleCount(type);
    }

    @Override
    public List<Map<String, Object>> selectArticleTrend() {
        return articleMapper.selectArticleTrend();
    }

    @Override
    public List<Map<String, Object>> selectCategoryStats() {
        return articleMapper.selectCategoryStats();
    }

    @Override
    public List<Map<String, Object>> selectTopArticles(int limit) {
        return articleMapper.selectTopArticles(limit);
    }

    @Override
    public int updateArticleCozeId(Long articleId, Long cozeId) {
        return articleMapper.updateArticleCozeId(articleId, cozeId);
    }
}