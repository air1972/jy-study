package com.jy.study.lesson.service.impl;

import com.jy.study.common.core.text.Convert;
import com.jy.study.common.exception.ServiceException;
import com.jy.study.common.utils.StringUtils;
import com.jy.study.lesson.domain.StudyComment;
import com.jy.study.lesson.mapper.StudyCommentMapper;
import com.jy.study.lesson.service.IStudyCommentService;
import com.jy.study.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class StudyCommentServiceImpl implements IStudyCommentService {
    private static final int DEFAULT_PAGE_SIZE = 5;
    private static final int MAX_PAGE_SIZE = 20;
    private static final int DEFAULT_MIN_INTERVAL_SECONDS = 15;
    private static final int DEFAULT_DUPLICATE_MINUTES = 10;
    private static final String DEFAULT_SENSITIVE_WORDS = "操你妈,傻逼,妈逼,色情交易,赌博平台,毒品交易,约炮,援交";

    @Autowired
    private StudyCommentMapper commentMapper;

    @Autowired
    private ISysConfigService configService;

    @Override
    public List<StudyComment> selectComments(String type, Long targetId, Integer pageNum, Integer pageSize) {
        if (targetId == null || StringUtils.isEmpty(type)) {
            return Collections.emptyList();
        }
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int offset = (safePageNum - 1) * safePageSize;
        return commentMapper.selectCommentListByTarget(type, targetId, offset, safePageSize);
    }

    @Override
    public Long countComments(String type, Long targetId) {
        if (targetId == null || StringUtils.isEmpty(type)) {
            return 0L;
        }
        Long count = commentMapper.countComments(type, targetId);
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyComment addComment(Long userId, String type, Long targetId, String content) {
        String normalizedContent = normalizeContent(content);
        validateComment(userId, type, targetId, normalizedContent);

        StudyComment comment = new StudyComment();
        comment.setUserId(userId);
        comment.setType(type);
        comment.setTargetId(targetId);
        comment.setContent(normalizedContent);
        comment.setStatus("0");
        comment.setCreateTime(new Date());
        commentMapper.insertComment(comment);
        return commentMapper.selectCommentById(comment.getCommentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOwnComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }
        return commentMapper.deleteOwnComment(commentId, userId) > 0;
    }

    @Override
    public List<StudyComment> selectCommentList(StudyComment comment) {
        return commentMapper.selectCommentList(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCommentStatus(Long commentId, String status, String updateBy) {
        if (commentId == null || StringUtils.isEmpty(status)) {
            return 0;
        }
        return commentMapper.updateCommentStatus(commentId, status, updateBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCommentStatusByIds(String ids, String status, String updateBy) {
        if (StringUtils.isEmpty(ids) || StringUtils.isEmpty(status)) {
            return 0;
        }
        Long[] commentIds = Convert.toLongArray(ids);
        if (commentIds == null || commentIds.length == 0) {
            return 0;
        }
        return commentMapper.updateCommentStatusByIds(commentIds, status, updateBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCommentByIds(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return 0;
        }
        Long[] commentIds = Convert.toLongArray(ids);
        if (commentIds == null || commentIds.length == 0) {
            return 0;
        }
        return commentMapper.deleteCommentByIds(commentIds);
    }

    private void validateComment(Long userId, String type, Long targetId, String content) {
        if (userId == null) {
            throw new ServiceException("请先登录");
        }
        if (StringUtils.isEmpty(type) || targetId == null) {
            throw new ServiceException("评论参数错误");
        }
        if (StringUtils.isEmpty(content)) {
            throw new ServiceException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new ServiceException("评论内容不能超过500个字符");
        }

        String contentWithoutSpace = content.replaceAll("\\s+", "");
        if (StringUtils.isEmpty(contentWithoutSpace)) {
            throw new ServiceException("评论内容不能为空白字符");
        }

        if (StringUtils.isNotEmpty(findSensitiveWord(content))) {
            throw new ServiceException("评论包含敏感词，请调整后再发布");
        }

        int minIntervalSeconds = getIntConfig("study.comment.minIntervalSeconds", DEFAULT_MIN_INTERVAL_SECONDS);
        StudyComment latestComment = commentMapper.selectLatestCommentByUser(userId);
        if (latestComment != null && latestComment.getCreateTime() != null) {
            long diffMillis = Math.abs(System.currentTimeMillis() - latestComment.getCreateTime().getTime());
            if (diffMillis < TimeUnit.SECONDS.toMillis(minIntervalSeconds)) {
                throw new ServiceException("评论过于频繁，请稍后再试");
            }
        }

        int duplicateMinutes = getIntConfig("study.comment.duplicateMinutes", DEFAULT_DUPLICATE_MINUTES);
        List<StudyComment> recentComments = commentMapper.selectRecentCommentsByUser(userId, type, targetId, duplicateMinutes);
        String compareValue = normalizeForCompare(content);
        for (StudyComment recentComment : recentComments) {
            if (recentComment == null || StringUtils.isEmpty(recentComment.getContent())) {
                continue;
            }
            if (compareValue.equals(normalizeForCompare(recentComment.getContent()))) {
                throw new ServiceException("请勿重复发布相同内容");
            }
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        normalized = normalized.replaceAll("[\\t\\x0B\\f]+", " ");
        return normalized.trim();
    }

    private String normalizeForCompare(String content) {
        return StringUtils.isEmpty(content) ? "" : content.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private String findSensitiveWord(String content) {
        String text = StringUtils.isEmpty(content) ? "" : content.toLowerCase(Locale.ROOT);
        for (String word : getSensitiveWords()) {
            if (StringUtils.isEmpty(word)) {
                continue;
            }
            if (text.contains(word.toLowerCase(Locale.ROOT))) {
                return word;
            }
        }
        return null;
    }

    private Set<String> getSensitiveWords() {
        String rawConfig = configService.selectConfigByKey("study.comment.sensitiveWords");
        String source = StringUtils.isNotEmpty(rawConfig) ? rawConfig : DEFAULT_SENSITIVE_WORDS;
        String[] parts = source.split("[,，;；\\r\\n]+");
        Set<String> words = new LinkedHashSet<>();
        for (String part : parts) {
            String word = part == null ? "" : part.trim();
            if (StringUtils.isNotEmpty(word)) {
                words.add(word);
            }
        }
        return words;
    }

    private int getIntConfig(String key, int defaultValue) {
        String value = configService.selectConfigByKey(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
