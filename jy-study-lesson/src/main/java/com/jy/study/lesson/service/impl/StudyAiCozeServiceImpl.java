package com.jy.study.lesson.service.impl;

import com.jy.study.common.core.text.Convert;
import com.jy.study.lesson.domain.StudyAiCoze;
import com.jy.study.lesson.mapper.StudyAiCozeMapper;
import com.jy.study.lesson.service.IStudyAiCozeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyAiCozeServiceImpl implements IStudyAiCozeService {
    @Autowired
    private StudyAiCozeMapper aiCozeMapper;

    @Override
    public StudyAiCoze selectAiCozeById(Long id) {
        return aiCozeMapper.selectAiCozeById(id);
    }

    @Override
    public List<StudyAiCoze> selectAiCozeList(StudyAiCoze aiCoze) {
        return aiCozeMapper.selectAiCozeList(aiCoze);
    }

    @Override
    public int insertAiCoze(StudyAiCoze aiCoze) {
        return aiCozeMapper.insertAiCoze(aiCoze);
    }

    @Override
    public int updateAiCoze(StudyAiCoze aiCoze) {
        return aiCozeMapper.updateAiCoze(aiCoze);
    }

    @Override
    public int deleteAiCozeById(Long id) {
        return aiCozeMapper.deleteAiCozeById(id);
    }

    @Override
    public int deleteAiCozeByIds(String ids) {
        return aiCozeMapper.deleteAiCozeByIds(Convert.toStrArray(ids));
    }

    @Override
    public StudyAiCoze selectLatestByArticleId(Long articleId) {
        return aiCozeMapper.selectLatestByArticleId(articleId);
    }
} 