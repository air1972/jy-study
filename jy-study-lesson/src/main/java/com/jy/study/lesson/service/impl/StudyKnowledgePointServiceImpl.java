package com.jy.study.lesson.service.impl;

import java.util.List;
import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyKnowledgePointMapper;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.lesson.service.IStudyKnowledgePointService;
import com.jy.study.common.core.text.Convert;

/**
 * 知识点Service业务层处理
 */
@Service
public class StudyKnowledgePointServiceImpl implements IStudyKnowledgePointService {
    @Autowired
    private StudyKnowledgePointMapper studyKnowledgePointMapper;

    /**
     * 查询知识点列表
     */
    @Override
    public List<StudyKnowledgePoint> selectStudyKnowledgePointList(StudyKnowledgePoint studyKnowledgePoint) {
        return studyKnowledgePointMapper.selectStudyKnowledgePointList(studyKnowledgePoint);
    }

    /**
     * 根据ID查询知识点
     */
    @Override
    public StudyKnowledgePoint selectStudyKnowledgePointById(Long id) {
        return studyKnowledgePointMapper.selectStudyKnowledgePointById(id);
    }

    /**
     * 新增知识点
     */
    @Override
    public int insertStudyKnowledgePoint(StudyKnowledgePoint studyKnowledgePoint) {
        studyKnowledgePoint.setCreateTime(DateUtils.getNowDate());
        return studyKnowledgePointMapper.insertStudyKnowledgePoint(studyKnowledgePoint);
    }

    /**
     * 修改知识点
     */
    @Override
    public int updateStudyKnowledgePoint(StudyKnowledgePoint studyKnowledgePoint) {
        studyKnowledgePoint.setUpdateTime(DateUtils.getNowDate());
        return studyKnowledgePointMapper.updateStudyKnowledgePoint(studyKnowledgePoint);
    }

    /**
     * 删除知识点
     */
    @Override
    public int deleteStudyKnowledgePointById(Long id) {
        return studyKnowledgePointMapper.deleteStudyKnowledgePointById(id);
    }

    /**
     * 批量删除知识点
     */
    @Override
    public int deleteStudyKnowledgePointByIds(String ids) {
        return studyKnowledgePointMapper.deleteStudyKnowledgePointByIds(Convert.toStrArray(ids));
    }
}
