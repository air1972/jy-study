package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyVersion;
import com.jy.study.lesson.mapper.StudyVersionMapper;
import com.jy.study.lesson.service.IStudyVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版本Service业务层处理
 */
@Service
public class StudyVersionServiceImpl implements IStudyVersionService {

    @Autowired
    private StudyVersionMapper studyVersionMapper;

    /**
     * 查询版本列表
     */
    @Override
    public List<StudyVersion> selectStudyVersionList(StudyVersion studyVersion) {
        return studyVersionMapper.selectStudyVersionList(studyVersion);
    }

    /**
     * 根据ID查询版本
     */
    @Override
    public StudyVersion selectStudyVersionById(Long versionId) {
        return studyVersionMapper.selectStudyVersionById(versionId);
    }

    /**
     * 新增版本
     */
    @Override
    public int insertStudyVersion(StudyVersion studyVersion) {
        studyVersion.setCreateTime(DateUtils.getNowDate());
        return studyVersionMapper.insertStudyVersion(studyVersion);
    }

    /**
     * 修改版本
     */
    @Override
    public int updateStudyVersion(StudyVersion studyVersion) {
        studyVersion.setUpdateTime(DateUtils.getNowDate());
        return studyVersionMapper.updateStudyVersion(studyVersion);
    }

    /**
     * 删除版本
     */
    @Override
    public int deleteStudyVersionById(Long versionId) {
        return studyVersionMapper.deleteStudyVersionById(versionId);
    }

    /**
     * 批量删除版本
     */
    @Override
    public int deleteStudyVersionByIds(String ids) {
        Long[] versionIds = com.jy.study.common.core.text.Convert.toLongArray(ids);
        return studyVersionMapper.deleteStudyVersionByIds(versionIds);
    }
}
