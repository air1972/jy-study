package com.jy.study.lesson.mapper;
import com.jy.study.lesson.domain.StudyArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyArticleMapper {
    public StudyArticle selectArticleById(Long articleId);

    /**
     * 查询文章列表
     * 
     * @param article 文章信息
     * @return 文章集合
     */
    public List<StudyArticle> selectArticleList(StudyArticle article);

    public int insertArticle(StudyArticle article);

    public int updateArticle(StudyArticle article);

    public int deleteArticleById(Long articleId);

    public int deleteArticleByIds(String[] articleIds);

    public int incrementViewCount(Long articleId);

    public int incrementLikeCount(Long articleId);

    public int decrementLikeCount(Long articleId);

    public int incrementCollectCount(Long articleId);

    public int decrementCollectCount(Long articleId);

    /**
     * 统计文章数量
     * @param type 统计类型
     * @return 文章数量
     */
    public Long selectArticleCount(String type);

    /**
     * 获取文章增长趋势
     * @return 最近6个月的文章数量统计
     */
    public List<Map<String, Object>> selectArticleTrend();

    /**
     * 统计文章分类数据
     * @return 分类及其数量
     */
    public List<Map<String, Object>> selectCategoryStats();

    /**
     * 获取热门文章
     * @param limit 获取数量
     * @return 文章列表
     */
    public List<Map<String, Object>> selectTopArticles(int limit);

    /**
     * 更新文章的AI试题ID
     * 
     * @param articleId 文章ID
     * @param cozeId AI试题ID
     * @return 结果
     */
    public int updateArticleCozeId(@Param("articleId") Long articleId, @Param("cozeId") Long cozeId);
} 