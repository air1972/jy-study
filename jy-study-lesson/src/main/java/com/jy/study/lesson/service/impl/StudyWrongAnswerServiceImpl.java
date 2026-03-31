package com.jy.study.lesson.service.impl;

import java.util.Date;
import java.util.List;
import com.jy.study.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jy.study.lesson.mapper.StudyWrongAnswerMapper;
import com.jy.study.lesson.domain.StudyWrongAnswer;
import com.jy.study.lesson.service.IStudyWrongAnswerService;

/**
 * 错题本Service业务层处理
 */
@Service
public class StudyWrongAnswerServiceImpl implements IStudyWrongAnswerService {
    @Autowired
    private StudyWrongAnswerMapper studyWrongAnswerMapper;

    /**
     * 查询错题列表
     */
    @Override
    public List<StudyWrongAnswer> selectStudyWrongAnswerList(StudyWrongAnswer studyWrongAnswer) {
        return studyWrongAnswerMapper.selectStudyWrongAnswerList(studyWrongAnswer);
    }

    /**
     * 新增错题
     */
    @Override
    public void addWrongAnswer(Long userId, Long exerciseId) {
        StudyWrongAnswer wrongAnswer = studyWrongAnswerMapper.selectStudyWrongAnswerByUserIdAndExerciseId(userId, exerciseId);
        if (wrongAnswer == null) {
            wrongAnswer = new StudyWrongAnswer();
            wrongAnswer.setUserId(userId);
            wrongAnswer.setExerciseId(exerciseId);
            wrongAnswer.setWrongCount(1);
            wrongAnswer.setCreateTime(DateUtils.getNowDate());
            wrongAnswer.setLastWrongTime(DateUtils.getNowDate());
            studyWrongAnswerMapper.insertStudyWrongAnswer(wrongAnswer);
        } else {
            wrongAnswer.setWrongCount(wrongAnswer.getWrongCount() + 1);
            wrongAnswer.setLastWrongTime(DateUtils.getNowDate());
            studyWrongAnswerMapper.updateStudyWrongAnswer(wrongAnswer);
        }
    }

    @Override
    public int deleteStudyWrongAnswerByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return studyWrongAnswerMapper.deleteStudyWrongAnswerByIds(ids);
    }

    @Override
    public int purgeInvalidWrongAnswers() {
        return studyWrongAnswerMapper.purgeInvalidWrongAnswers();
    }
}
