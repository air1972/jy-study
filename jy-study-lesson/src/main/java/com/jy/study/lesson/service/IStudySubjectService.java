package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudySubject;
import java.util.List;

/**
 * 科目Service接口
 */
public interface IStudySubjectService {
    /**
     * 查询科目列表
     */
    public List<StudySubject> selectStudySubjectList(StudySubject studySubject);

    /**
     * 根据ID查询科目
     */
    public StudySubject selectStudySubjectById(Long subjectId);

    /**
     * 新增科目
     */
    public int insertStudySubject(StudySubject studySubject);

    /**
     * 修改科目
     */
    public int updateStudySubject(StudySubject studySubject);

    /**
     * 删除科目
     */
    public int deleteStudySubjectById(Long subjectId);

    /**
     * 批量删除科目
     */
    public int deleteStudySubjectByIds(String ids);
}
