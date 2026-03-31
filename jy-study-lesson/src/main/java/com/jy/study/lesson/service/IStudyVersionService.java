package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyVersion;
import java.util.List;

/**
 * 版本Service接口
 */
public interface IStudyVersionService {
    /**
     * 查询版本列表
     */
    public List<StudyVersion> selectStudyVersionList(StudyVersion studyVersion);

    /**
     * 根据ID查询版本
     */
    public StudyVersion selectStudyVersionById(Long versionId);

    /**
     * 新增版本
     */
    public int insertStudyVersion(StudyVersion studyVersion);

    /**
     * 修改版本
     */
    public int updateStudyVersion(StudyVersion studyVersion);

    /**
     * 删除版本
     */
    public int deleteStudyVersionById(Long versionId);

    /**
     * 批量删除版本
     */
    public int deleteStudyVersionByIds(String ids);
}
