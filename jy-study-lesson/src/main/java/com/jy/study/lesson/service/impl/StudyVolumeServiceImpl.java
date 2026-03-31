package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.DateUtils;
import com.jy.study.lesson.domain.StudyVolume;
import com.jy.study.lesson.mapper.StudyVolumeMapper;
import com.jy.study.lesson.service.IStudyVolumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 册子Service业务层处理
 */
@Service
public class StudyVolumeServiceImpl implements IStudyVolumeService {

    @Autowired
    private StudyVolumeMapper studyVolumeMapper;

    /**
     * 查询册子列表
     */
    @Override
    public List<StudyVolume> selectStudyVolumeList(StudyVolume studyVolume) {
        return studyVolumeMapper.selectStudyVolumeList(studyVolume);
    }

    /**
     * 根据ID查询册子
     */
    @Override
    public StudyVolume selectStudyVolumeById(Long volumeId) {
        return studyVolumeMapper.selectStudyVolumeById(volumeId);
    }

    /**
     * 新增册子
     */
    @Override
    public int insertStudyVolume(StudyVolume studyVolume) {
        studyVolume.setCreateTime(DateUtils.getNowDate());
        return studyVolumeMapper.insertStudyVolume(studyVolume);
    }

    /**
     * 修改册子
     */
    @Override
    public int updateStudyVolume(StudyVolume studyVolume) {
        studyVolume.setUpdateTime(DateUtils.getNowDate());
        return studyVolumeMapper.updateStudyVolume(studyVolume);
    }

    /**
     * 删除册子
     */
    @Override
    public int deleteStudyVolumeById(Long volumeId) {
        return studyVolumeMapper.deleteStudyVolumeById(volumeId);
    }

    /**
     * 批量删除册子
     */
    @Override
    public int deleteStudyVolumeByIds(String ids) {
        Long[] volumeIds = com.jy.study.common.core.text.Convert.toLongArray(ids);
        return studyVolumeMapper.deleteStudyVolumeByIds(volumeIds);
    }
}
