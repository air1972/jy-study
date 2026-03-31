package com.jy.study.lesson.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.jy.study.lesson.domain.StudyUserView;
import com.jy.study.lesson.domain.dto.UserViewHistoryDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudyUserViewMapper {
    public int insertUserView(StudyUserView view);
    
    public StudyUserView selectUserView(@Param("userId") Long userId, 
                                      @Param("type") String type, 
                                      @Param("targetId") Long targetId);
    
    public int deleteUserView(Long viewId);
    
    /**
     * 查询用户浏览历史记录（简单连表，仅获取标题信息）
     */
    public List<UserViewHistoryDTO> selectUserViewHistory(@Param("userId") Long userId, @Param("limit") int limit);


    /**
     * 获取用户浏览历史记录（连表查询详细版）
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 浏览历史记录列表
     */
    public List<UserViewHistoryDTO> selectUserViewHistoryWithDetails(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 获取最近7天活跃用户信息
     * @return 用户活跃信息列表
     */
    public List<Map<String, Object>> selectRecentActiveUsers();

}