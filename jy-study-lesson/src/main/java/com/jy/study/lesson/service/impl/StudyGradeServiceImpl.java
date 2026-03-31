package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyGrade;
import com.jy.study.lesson.mapper.StudyGradeMapper;
import com.jy.study.lesson.service.IStudyGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 年级Service业务层处理
 */
@Service
public class StudyGradeServiceImpl implements IStudyGradeService {

    @Autowired
    private StudyGradeMapper studyGradeMapper;

    /**
     * 查询年级列表
     */
    @Override
    public List<StudyGrade> selectStudyGradeList(StudyGrade studyGrade) {
        return studyGradeMapper.selectStudyGradeList(studyGrade);
    }

    /**
     * 根据ID查询年级
     */
    @Override
    public StudyGrade selectStudyGradeById(Long gradeId) {
        return studyGradeMapper.selectStudyGradeById(gradeId);
    }

    /**
     * 新增年级
     */
    @Override
    public int insertStudyGrade(StudyGrade studyGrade) {
        studyGrade.setCreateTime(DateUtils.getNowDate());
        return studyGradeMapper.insertStudyGrade(studyGrade);
    }

    /**
     * 修改年级
     */
    @Override
    public int updateStudyGrade(StudyGrade studyGrade) {
        studyGrade.setUpdateTime(DateUtils.getNowDate());
        return studyGradeMapper.updateStudyGrade(studyGrade);
    }

    /**
     * 删除年级
     */
    @Override
    public int deleteStudyGradeById(Long gradeId) {
        return studyGradeMapper.deleteStudyGradeById(gradeId);
    }

    /**
     * 批量删除年级
     */
    @Override
    public int deleteStudyGradeByIds(String ids) {
        Long[] gradeIds = com.jy.study.common.core.text.Convert.toLongArray(ids);
        return studyGradeMapper.deleteStudyGradeByIds(gradeIds);
    }
}
