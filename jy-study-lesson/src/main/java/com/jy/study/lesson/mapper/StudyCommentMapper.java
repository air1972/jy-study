package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyCommentMapper {
    int insertComment(StudyComment comment);

    StudyComment selectCommentById(@Param("commentId") Long commentId);

    List<StudyComment> selectCommentListByTarget(@Param("type") String type,
                                                 @Param("targetId") Long targetId,
                                                 @Param("offset") Integer offset,
                                                 @Param("limit") Integer limit);

    Long countComments(@Param("type") String type, @Param("targetId") Long targetId);

    List<StudyComment> selectCommentList(StudyComment comment);

    int updateCommentStatus(@Param("commentId") Long commentId,
                            @Param("status") String status,
                            @Param("updateBy") String updateBy);

    int updateCommentStatusByIds(@Param("commentIds") Long[] commentIds,
                                 @Param("status") String status,
                                 @Param("updateBy") String updateBy);

    int deleteOwnComment(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int deleteCommentByIds(@Param("commentIds") Long[] commentIds);

    StudyComment selectLatestCommentByUser(@Param("userId") Long userId);

    List<StudyComment> selectRecentCommentsByUser(@Param("userId") Long userId,
                                                  @Param("type") String type,
                                                  @Param("targetId") Long targetId,
                                                  @Param("minutes") Integer minutes);
}
