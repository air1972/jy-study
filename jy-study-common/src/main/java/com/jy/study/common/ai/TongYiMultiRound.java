package com.jy.study.common.ai;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class TongYiMultiRound {
    private static final Logger log = LoggerFactory.getLogger(TongYiMultiRound.class);

    @Value("${modelTongyi.apiKey}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Constants.apiKey = apiKey;
    }

    /**
     * 创建生成参数对象
     *
     * @param messages 对话消息列表，包含与用户和AI助手的对话历史
     * @return 返回构建好的GenerationParam对象
     */
    public GenerationParam createGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .model(Generation.Models.QWEN_PLUS) // 使用枚举常量替代字符串
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .build();
    }

    /**
     * 根据提供的参数调用生成模型，并返回生成的结果
     */
    public GenerationResult callGenerationWithMessages(GenerationParam param) throws ApiException, NoApiKeyException, InputRequiredException {
        try {
            Generation gen = new Generation();
            return gen.call(param);
        } catch (Exception e) {
            log.error("调用通义千问API出错", e);
            throw e;
        }
    }

    /**
     * 创建系统消息
     */
    public Message createSystemMessage() {
        return Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一个专业的学习助手，擅长解答各类学习问题。你应该：1. 提供准确、清晰的解答 2. 使用适合学习的语气和表达方式 3. 在必要时给出例子和详细说明 4. 鼓励学习者思考和探索 5. 保持耐心和友善的态度")
                .build();
    }

    /**
     * 创建用户消息
     */
    public Message createUserMessage(String content) {
        return Message.builder()
                .role(Role.USER.getValue())
                .content(content)
                .build();
    }

    /**
     * 创建助手消息
     */
    public Message createAssistantMessage(String content) {
        return Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content(content)
                .build();
    }

    /**
     * 测试API是否正常工作
     */
    public boolean testConnection() {
        try {
            List<Message> messages = new ArrayList<>();
            messages.add(createSystemMessage());
            messages.add(createUserMessage("你好"));
            
            GenerationParam param = createGenerationParam(messages);
            GenerationResult result = callGenerationWithMessages(param);
            
            return result != null && result.getOutput() != null;
        } catch (Exception e) {
            log.error("API连接测试失败", e);
            return false;
        }
    }

    /**
     * 创建流式生成参数
     */
    public GenerationParam createStreamGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .model(Generation.Models.QWEN_TURBO)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .incrementalOutput(true) // 启用增量输出
                .build();
    }

    /**
     * 执行流式调用
     */
    public void streamCall(GenerationParam param, ResultCallback<GenerationResult> callback) 
            throws NoApiKeyException, ApiException, InputRequiredException {
        try {
            Generation gen = new Generation();
            // 使用 Flowable 方式进行流式调用
            gen.streamCall(param)
               .subscribe(
                   // onNext
                   message -> {
                       if (callback != null) {
                           callback.onEvent(message);
                       }
                   },
                   // onError
                   error -> {
                       if (callback != null) {
                           callback.onError(error instanceof Exception ? (Exception) error : new Exception(error));
                       }
                   },
                   // onComplete
                   () -> {
                       if (callback != null) {
                           callback.onComplete();
                       }
                   }
               );
        } catch (Exception e) {
            log.error("流式调用出错", e);
            throw e;
        }
    }
}