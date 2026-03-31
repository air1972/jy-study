package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyVolume;
import java.util.List;

/**
 * 册子Service接口
 */
public interface IStudyVolumeService {
    /**
     * 查询册子列表
     */
    public List<StudyVolume> selectStudyVolumeList(StudyVolume studyVolume);

    /**
     * 根据ID查询册子
     */
    public StudyVolume selectStudyVolumeById(Long volumeId);

    /**
     * 新增册子
     */
    public int insertStudyVolume(StudyVolume studyVolume);

    /**
     * 修改册子
     */
    public int updateStudyVolume(StudyVolume studyVolume);

    /**
     * 删除册子
     */
    public int deleteStudyVolumeById(Long volumeId);

    /**
     * 批量删除册子
     */
    public int deleteStudyVolumeByIds(String ids);
}
