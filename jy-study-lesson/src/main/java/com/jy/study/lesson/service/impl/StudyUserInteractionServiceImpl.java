package com.jy.study.lesson.service.impl;

import com.jy.study.common.utils.ShiroUtils;
import com.jy.study.lesson.mapper.StudyUserViewMapper;
import com.jy.study.lesson.mapper.StudyUserLikeMapper;
import com.jy.study.lesson.mapper.StudyUserCollectMapper;
import com.jy.study.lesson.mapper.StudyArticleMapper;
import com.jy.study.lesson.mapper.StudyLessonMapper;
import com.jy.study.lesson.domain.StudyUserView;
import com.jy.study.lesson.domain.StudyUserLike;
import com.jy.study.lesson.domain.StudyUserCollect;
import com.jy.study.lesson.service.IStudyUserInteractionService;
import com.jy.study.lesson.domain.dto.UserViewHistoryDTO;
import com.jy.study.lesson.domain.dto.UserCollectionDTO;
import com.jy.study.lesson.domain.dto.UserLikeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * 用户交互Service实现
 */
@Service
public class StudyUserInteractionServiceImpl implements IStudyUserInteractionService {
    
    @Autowired
    private StudyUserViewMapper viewMapper;
    
    @Autowired
    private StudyUserLikeMapper likeMapper;
    
    @Autowired
    private StudyUserCollectMapper collectMapper;

    @Autowired
    private StudyArticleMapper articleMapper;

    @Autowired
    private StudyLessonMapper lessonMapper;

    @Autowired
    private StudyUserViewMapper userViewMapper;

    @Override
    public void recordView(String type, Long targetId) {
        // 无论是否登录都记录浏览量
        if ("1".equals(type)) {
            lessonMapper.incrementViewCount(targetId);
        } else if ("2".equals(type)) {
            articleMapper.incrementViewCount(targetId);
        }
        // 获取当前用户ID(使用Shiro)
        Long userId = ShiroUtils.getUserId();
        // 获取IP地址(使用Shiro工具类)
        String ipAddr = ShiroUtils.getIp();
        
        // 只有登录用户才记录详细的浏览记录
        if (userId != null) {
            StudyUserView view = new StudyUserView();
            view.setUserId(userId);
            view.setType(type);
            view.setTargetId(targetId);
            view.setIpAddr(ipAddr);
            view.setCreateTime(new Date());
            viewMapper.insertUserView(view);
        }
    }

    @Override
    @Transactional
    public boolean like(Long userId, String type, Long targetId) {
        if (checkLiked(userId, type, targetId)) {
            return false;
        }
        
        StudyUserLike like = new StudyUserLike();
        like.setUserId(userId);
        like.setType(type);
        like.setTargetId(targetId);
        like.setCreateTime(new Date());
        likeMapper.insertUserLike(like);
        
        // 更新目标对象的点赞数
        if ("1".equals(type)) {
            lessonMapper.incrementLikeCount(targetId);
        } else if ("2".equals(type)) {
            articleMapper.incrementLikeCount(targetId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean unlike(Long userId, String type, Long targetId) {
        if (!checkLiked(userId, type, targetId)) {
            return false;
        }
        
        likeMapper.deleteUserLike(userId, type, targetId);
        
        // 更新目标对象的点赞数
        if ("1".equals(type)) {
            lessonMapper.decrementLikeCount(targetId);
        } else if ("2".equals(type)) {
            articleMapper.decrementLikeCount(targetId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean collect(Long userId, String type, Long targetId) {
        if (checkCollected(userId, type, targetId)) {
            return false;
        }
        
        StudyUserCollect collect = new StudyUserCollect();
        collect.setUserId(userId);
        collect.setType(type);
        collect.setTargetId(targetId);
        collect.setCreateTime(new Date());
        collectMapper.insertUserCollect(collect);
        
        // 更新目标对象的收藏数
        if ("1".equals(type)) {
            lessonMapper.incrementCollectCount(targetId);
        } else if ("2".equals(type)) {
            articleMapper.incrementCollectCount(targetId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean uncollect(Long userId, String type, Long targetId) {
        if (!checkCollected(userId, type, targetId)) {
            return false;
        }
        
        collectMapper.deleteUserCollect(userId, type, targetId);
        
        // 更新目标对象的收藏数
        if ("1".equals(type)) {
            lessonMapper.decrementCollectCount(targetId);
        } else if ("2".equals(type)) {
            articleMapper.decrementCollectCount(targetId);
        }
        return true;
    }

    @Override
    public boolean checkLiked(Long userId, String type, Long targetId) {
        // 未登录用户默认未点赞
        if (userId == null) {
            return false;
        }
        return likeMapper.checkLiked(userId, type, targetId);
    }

    @Override
    public boolean checkCollected(Long userId, String type, Long targetId) {
        // 未登录用户默认未收藏
        if (userId == null) {
            return false;
        }
        return collectMapper.checkCollected(userId, type, targetId);
    }

    @Override
    public List<UserViewHistoryDTO> getUserViewHistory(Long userId, int limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return viewMapper.selectUserViewHistory(userId, limit);
    }

    /**
     * 获取用户浏览历史记录（包含详细信息）
     */
    @Override
    public List<UserViewHistoryDTO> getUserViewHistoryWithDetails(Long userId, int limit) {
        return viewMapper.selectUserViewHistoryWithDetails(userId, limit);
    }

    @Override
    public List<UserCollectionDTO> getUserCollectionWithDetails(Long userId, int limit) {
        return collectMapper.selectUserCollectionWithDetails(userId, limit);
    }

    @Override
    public List<UserLikeDTO> getUserLikeWithDetails(Long userId, int limit) {
        return likeMapper.selectUserLikeWithDetails(userId, limit);
    }


    @Override
    public List<Map<String, Object>> selectRecentActiveUsers() {
        return userViewMapper.selectRecentActiveUsers();
    }
}