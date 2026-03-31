package com.jy.study.common.ai;

import java.util.ArrayList;
import java.util.List;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Value;

public class AliTongYiModel {

    @Value("${modelTongyi.apiKey}")
    private static String apiKey;

    public static String callWithMessage()
            throws NoApiKeyException, ApiException, InputRequiredException {
        Generation gen = new Generation();
        List<Message> messages = new ArrayList<>();
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content("You are a helpful assistant.").build();
        Message userMsg = Message.builder().role(Role.USER.getValue()).content("请介绍一下通义千问").build();
        messages.add(systemMsg);
        messages.add(userMsg);
        // 构建生成参数对象，指定模型为QWEN_TURBO，并设置消息列表和结果格式
        GenerationParam param =
                GenerationParam.builder().model(Generation.Models.QWEN_TURBO).messages(messages)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
        // 调用Generation对象的call方法，传入生成参数，获取生成结果
        GenerationResult result = gen.call(param);
        
        // 从结果中提取content内容
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }


    public static void main(String[] args) {
        // 以下赋值语句请放在类或方法中运行
        Constants.apiKey= apiKey;
        // 没有这行报错无apiKey

        try {
            String string=callWithMessage();
            System.out.println("string");
            System.out.println(string);

        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

}
