package com.jy.study.lesson.mapper;

import com.jy.study.lesson.domain.StudyUserLike;
import com.jy.study.lesson.domain.dto.UserLikeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyUserLikeMapper {
    public int insertUserLike(StudyUserLike like);
    
    public StudyUserLike selectUserLike(@Param("userId") Long userId, @Param("type") String type, @Param("targetId") Long targetId);

    public List<UserLikeDTO> selectUserLikeWithDetails (@Param("userId") Long userId, @Param("limit") int limit);

    public int deleteUserLike(@Param("userId") Long userId, @Param("type") String type, @Param("targetId") Long targetId);
    
    public boolean checkLiked(@Param("userId") Long userId, @Param("type") String type, @Param("targetId") Long targetId);
}
