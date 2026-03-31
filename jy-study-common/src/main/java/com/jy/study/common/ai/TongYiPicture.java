package com.jy.study.common.ai;
// Copyright (c) Alibaba, Inc. and its affiliates.

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TongYiPicture {
    @Value("${modelTongyi.apiKey}")
    private String apiKey;

    public String generaPic(String title) throws ApiException, NoApiKeyException {
        String basePrompt = "生成适合学习网站的封面图片，要求如下：比例：4:3，风格：科技感：使用现代、未来感的设计元素。简洁：保持设计简洁，突出重点。色彩鲜明：使用明亮、对比强烈的色彩。图形化：使用图形、图标和插图来传达信息。用简约现代扁平风格绘制封面图。";
        String prompt = basePrompt + " 文章主题是：" + title;
        
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(apiKey)
                .model("wanx2.1-t2i-turbo")
                .prompt(prompt)
                .negativePrompt("**图片内不包含文字、中文、任何字符！**")
                .n(1)
                .size("800*600")
                .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = imageSynthesis.call(param);
        
        // 从返回结果中提取URL
        if (result != null && result.getOutput() != null 
            && result.getOutput().getResults() != null 
            && !result.getOutput().getResults().isEmpty()) {
            return result.getOutput().getResults().get(0).get("url");
        }
        
        throw new RuntimeException("图片生成失败");
    }

    public static void listTask() throws ApiException, NoApiKeyException {
        ImageSynthesis is = new ImageSynthesis();
        AsyncTaskListParam param = AsyncTaskListParam.builder().build();
        ImageSynthesisListResult result = is.list(param);
        System.out.println(result);
    }

    public void fetchTask() throws ApiException, NoApiKeyException {
        String taskId = "your task id";
        ImageSynthesis is = new ImageSynthesis();
        // If set DASHSCOPE_API_KEY environment variable, apiKey can null.
        ImageSynthesisResult result = is.fetch(taskId, null);
        System.out.println(result.getOutput());
        System.out.println(result.getUsage());
    }

    public static void main(String[] args){
//        try{
            //generaPic();
            //listTask();
//        }catch(ApiException|NoApiKeyException e){
//            System.out.println(e.getMessage());
//        }
    }
}