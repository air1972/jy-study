package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyAiCoze;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudyAiCozeMapper {
    /**
     * 查询AI试题生成记录
     * 
     * @param id 记录ID
     * @return AI试题生成记录
     */
    public StudyAiCoze selectAiCozeById(Long id);

    /**
     * 查询AI试题生成记录列表
     * 
     * @param aiCoze AI试题生成记录
     * @return AI试题生成记录集合
     */
    public List<StudyAiCoze> selectAiCozeList(StudyAiCoze aiCoze);

    /**
     * 新增AI试题生成记录
     * 
     * @param aiCoze AI试题生成记录
     * @return 结果
     */
    public int insertAiCoze(StudyAiCoze aiCoze);

    /**
     * 修改AI试题生成记录
     * 
     * @param aiCoze AI试题生成记录
     * @return 结果
     */
    public int updateAiCoze(StudyAiCoze aiCoze);

    /**
     * 删除AI试题生成记录
     * 
     * @param id 记录ID
     * @return 结果
     */
    public int deleteAiCozeById(Long id);

    /**
     * 批量删除AI试题生成记录
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteAiCozeByIds(String[] ids);

    /**
     * 根据文章ID查询最新的AI试题记录
     * 
     * @param articleId 文章ID
     * @return AI试题生成记录
     */
    public StudyAiCoze selectLatestByArticleId(Long articleId);
} 