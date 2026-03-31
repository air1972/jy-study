package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyWrongAnswer;
import java.util.List;

/**
 * 错题本Service接口
 */
public interface IStudyWrongAnswerService {
    /**
     * 查询错题列表
     */
    public List<StudyWrongAnswer> selectStudyWrongAnswerList(StudyWrongAnswer studyWrongAnswer);

    /**
     * 新增错题
     */
    public void addWrongAnswer(Long userId, Long exerciseId);

    /**
     * 批量删除错题记录
     */
    public int deleteStudyWrongAnswerByIds(List<Long> ids);

    /**
     * 自动清理已删除题目的错题记录
     *
     * 规则：
     * 1. exercise_id > 0 关联 study_exercise.id
     * 2. exercise_id < 0 关联 study_lesson_exercise.exercise_id（取绝对值）
     *
     * @return 清理条数
     */
    public int purgeInvalidWrongAnswers();
}
