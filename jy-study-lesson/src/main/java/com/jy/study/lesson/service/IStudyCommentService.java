package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.StudyComment;

import java.util.List;

public interface IStudyCommentService {
    List<StudyComment> selectComments(String type, Long targetId, Integer pageNum, Integer pageSize);

    Long countComments(String type, Long targetId);

    StudyComment addComment(Long userId, String type, Long targetId, String content);

    boolean deleteOwnComment(Long commentId, Long userId);

    List<StudyComment> selectCommentList(StudyComment comment);

    int updateCommentStatus(Long commentId, String status, String updateBy);

    int updateCommentStatusByIds(String ids, String status, String updateBy);

    int deleteCommentByIds(String ids);
}
