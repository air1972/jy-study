package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyGrade;
import java.util.List;

/**
 * 年级Service接口
 */
public interface IStudyGradeService {
    /**
     * 查询年级列表
     */
    public List<StudyGrade> selectStudyGradeList(StudyGrade studyGrade);

    /**
     * 根据ID查询年级
     */
    public StudyGrade selectStudyGradeById(Long gradeId);

    /**
     * 新增年级
     */
    public int insertStudyGrade(StudyGrade studyGrade);

    /**
     * 修改年级
     */
    public int updateStudyGrade(StudyGrade studyGrade);

    /**
     * 删除年级
     */
    public int deleteStudyGradeById(Long gradeId);

    /**
     * 批量删除年级
     */
    public int deleteStudyGradeByIds(String ids);
}
