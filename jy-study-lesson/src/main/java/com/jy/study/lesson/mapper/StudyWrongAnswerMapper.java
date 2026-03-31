package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyWrongAnswer;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 错题本Mapper接口
 */
public interface StudyWrongAnswerMapper {
    /**
     * 查询错题列表
     */
    public List<StudyWrongAnswer> selectStudyWrongAnswerList(StudyWrongAnswer studyWrongAnswer);

    /**
     * 新增错题
     */
    public int insertStudyWrongAnswer(StudyWrongAnswer studyWrongAnswer);

    /**
     * 修改错题
     */
    public int updateStudyWrongAnswer(StudyWrongAnswer studyWrongAnswer);

    /**
     * 根据用户ID和题目ID查询错题
     */
    public StudyWrongAnswer selectStudyWrongAnswerByUserIdAndExerciseId(@Param("userId") Long userId, @Param("exerciseId") Long exerciseId);

    /**
     * 按主键批量删除错题
     */
    public int deleteStudyWrongAnswerByIds(@Param("ids") List<Long> ids);

    /**
     * 清理已删除题目的错题记录
     */
    public int purgeInvalidWrongAnswers();
}
