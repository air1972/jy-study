package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyKnowledgePoint;
import java.util.List;

/**
 * 知识点Service接口
 */
public interface IStudyKnowledgePointService {
    /**
     * 查询知识点列表
     */
    public List<StudyKnowledgePoint> selectStudyKnowledgePointList(StudyKnowledgePoint studyKnowledgePoint);

    /**
     * 根据ID查询知识点
     */
    public StudyKnowledgePoint selectStudyKnowledgePointById(Long id);

    /**
     * 新增知识点
     */
    public int insertStudyKnowledgePoint(StudyKnowledgePoint studyKnowledgePoint);

    /**
     * 修改知识点
     */
    public int updateStudyKnowledgePoint(StudyKnowledgePoint studyKnowledgePoint);

    /**
     * 删除知识点
     */
    public int deleteStudyKnowledgePointById(Long id);

    /**
     * 批量删除知识点
     */
    public int deleteStudyKnowledgePointByIds(String ids);
}
