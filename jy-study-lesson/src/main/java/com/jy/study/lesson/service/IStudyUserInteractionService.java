package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.dto.UserViewHistoryDTO;
import com.jy.study.lesson.domain.dto.UserCollectionDTO;
import com.jy.study.lesson.domain.dto.UserLikeDTO;

import java.util.List;
import java.util.Map;

public interface IStudyUserInteractionService {
    /**
     * 记录浏览
     */
    void recordView(String type, Long targetId);

    /**
     * 点赞
     */
    boolean like(Long userId, String type, Long targetId);

    /**
     * 取消点赞
     */
    boolean unlike(Long userId, String type, Long targetId);

    /**
     * 收藏
     */
    boolean collect(Long userId, String type, Long targetId);

    /**
     * 取消收藏
     */
    boolean uncollect(Long userId, String type, Long targetId);

    /**
     * 检查是否已点赞
     */
    boolean checkLiked(Long userId, String type, Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean checkCollected(Long userId, String type, Long targetId);

    /**
     * 获取用户浏览历史
     */
    List<UserViewHistoryDTO> getUserViewHistory(Long userId, int limit);

    /**
     * 获取用户浏览历史记录（包含详细信息）
     * 
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 浏览历史记录列表
     */
    public List<UserViewHistoryDTO> getUserViewHistoryWithDetails(Long userId, int limit);

    /**
     * 获取用户收藏记录（包含详细信息）
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 收藏记录列表
     */
    List<UserCollectionDTO> getUserCollectionWithDetails(Long userId, int limit);

    /**
     * 获取用户点赞记录（包含详细信息）
     *
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 点赞记录列表
     */
    List<UserLikeDTO> getUserLikeWithDetails(Long userId, int limit);


    List<Map<String, Object>> selectRecentActiveUsers() ;
} 