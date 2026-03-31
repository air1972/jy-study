package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudySubject;
import com.jy.study.lesson.mapper.StudySubjectMapper;
import com.jy.study.lesson.service.IStudySubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科目Service业务层处理
 */
@Service
public class StudySubjectServiceImpl implements IStudySubjectService {

    @Autowired
    private StudySubjectMapper studySubjectMapper;

    /**
     * 查询科目列表
     */
    @Override
    public List<StudySubject> selectStudySubjectList(StudySubject studySubject) {
        return studySubjectMapper.selectStudySubjectList(studySubject);
    }

    /**
     * 根据ID查询科目
     */
    @Override
    public StudySubject selectStudySubjectById(Long subjectId) {
        return studySubjectMapper.selectStudySubjectById(subjectId);
    }

    /**
     * 新增科目
     */
    @Override
    public int insertStudySubject(StudySubject studySubject) {
        studySubject.setCreateTime(DateUtils.getNowDate());
        return studySubjectMapper.insertStudySubject(studySubject);
    }

    /**
     * 修改科目
     */
    @Override
    public int updateStudySubject(StudySubject studySubject) {
        studySubject.setUpdateTime(DateUtils.getNowDate());
        return studySubjectMapper.updateStudySubject(studySubject);
    }

    /**
     * 删除科目
     */
    @Override
    public int deleteStudySubjectById(Long subjectId) {
        return studySubjectMapper.deleteStudySubjectById(subjectId);
    }

    /**
     * 批量删除科目
     */
    @Override
    public int deleteStudySubjectByIds(String ids) {
        Long[] subjectIds = com.jy.study.common.core.text.Convert.toLongArray(ids);
        return studySubjectMapper.deleteStudySubjectByIds(subjectIds);
    }
}
