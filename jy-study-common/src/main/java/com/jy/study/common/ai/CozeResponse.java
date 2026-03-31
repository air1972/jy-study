package com.jy.study.common.ai;

import lombok.Data;

/**
 * Coze API响应结果封装类
 */
@Data
public class CozeResponse {
    /** 生成的题目内容 */
    private String output;
    
    /** 文件URL */
    private String fileUrl;
    
    /** 调试URL */
    private String debugUrl;
} 