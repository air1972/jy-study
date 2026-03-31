package com.jy.study.lesson.service;

import com.jy.study.lesson.domain.LearningAnalysis;
import com.jy.study.lesson.domain.LearningPath;

/**
 * 学习分析服务接口
 */
public interface ILearningAnalysisService {

    /**
     * 分析用户学习情况
     * @param userId 用户ID
     * @return 学习分析结果
     */
    LearningAnalysis analyzeUserLearning(Long userId);

    /**
     * 生成个性化学习路径
     * @param userId 用户ID
     * @return 个性化学习路径
     */
    LearningPath generateLearningPath(Long userId);
}
