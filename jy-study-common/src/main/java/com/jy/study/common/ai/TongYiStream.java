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
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Value;

public class TongYiStream {

    @Value("${modelTongyi.apiKey}")
    private static String apiKey;
    /**
     * 处理代码生成结果
     * 将生成的结果消息内容追加到完整内容字符串中，并记录日志
     *
     * @param message 生成结果对象，包含生成的消息内容
     * @param fullContent 用于存储追加生成内容的字符串构建器
     */
    private static void handleGenerationResult(GenerationResult message, StringBuilder fullContent) {
        // 追加生成的消息内容到完整内容字符串中
        fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
        // 记录生成结果消息的日志

        }


    /**
     * 使用流式调用进行消息处理
     *
     * 本函数旨在通过流式调用的方式，处理并生成针对输入消息的回复或操作结果
     * 它首先构建一个生成参数对象，然后使用此参数进行流式调用，最后将得到的结果进行处理和记录
     *
     * @param gen 生成器对象，用于进行流式调用
     * @param userMsg 用户消息对象，包含需要处理或回复的用户输入
     */
    public static void streamCallWithMessage(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException {
        // 构建生成参数对象，用于后续的流式调用
        GenerationParam param = buildGenerationParam(userMsg);

        // 执行流式调用，并获取结果流
        Flowable<GenerationResult> result = gen.streamCall(param);

        // 使用StringBuilder来拼接完整的处理结果内容
        StringBuilder fullContent = new StringBuilder();

        // 对流式调用返回的每一个结果进行处理，将其内容拼接到fullContent中
        result.blockingForEach(message -> handleGenerationResult(message, fullContent));

        // 记录完整的处理结果内容

    }


    /**
     * 使用回调函数实现异步流式调用生成接口
     *
     * @param gen 生成接口实例，用于发起生成请求
     * @param userMsg 用户消息对象，包含生成请求所需的输入参数
     * @throws NoApiKeyException 当API密钥未设置时抛出此异常
     * @throws ApiException 当API调用过程中出现错误时抛出此异常
     * @throws InputRequiredException 当必要的输入参数缺失时抛出此异常
     * @throws InterruptedException 当线程在等待状态时被中断时抛出此异常
     */
    public static void streamCallWithCallback(Generation gen, Message userMsg)
            throws NoApiKeyException, ApiException, InputRequiredException, InterruptedException {
        // 构建生成请求参数
        GenerationParam param = buildGenerationParam(userMsg);
        // 创建一个不可重入的信号量，初始值为0，用于在异步操作完成后继续执行
        Semaphore semaphore = new Semaphore(0);
        // 用于存储生成的完整内容
        StringBuilder fullContent = new StringBuilder();

        // 异步调用生成接口，并处理生成结果、错误和完成状态
        gen.streamCall(param, new ResultCallback<GenerationResult>() {
            @Override
            public void onEvent(GenerationResult message) {
                // 处理每次生成结果，将其追加到完整内容中
                handleGenerationResult(message, fullContent);
            }

            @Override
            public void onError(Exception e) {
                // 记录错误信息，并释放信号量以便继续执行
                System.out.println(e.getMessage());
                semaphore.release();
            }

            @Override
            public void onComplete() {
                // 当生成完成时记录信息，并释放信号量以便继续执行

                semaphore.release();
            }
        });

        // 等待异步生成任务完成，然后记录完整生成内容
        semaphore.acquire();

    }

    /**
     * 构建对话生成参数
     */
    private static GenerationParam buildGenerationParam(Message userMsg) {
        // 使用 GenerationParam 构建器构建对话生成参数对象
        return GenerationParam.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                // 设置是否启用增量输出，便于实时展示生成过程
                .incrementalOutput(true)
                .build();
    }


    public static void main(String[] args) {
        Constants.apiKey= apiKey;
        try {
            Generation gen = new Generation();
            Message userMsg = Message.builder().role(Role.USER.getValue()).content("如何做西红柿炖牛腩？").build();

//            streamCallWithMessage(gen, userMsg);
            streamCallWithCallback(gen, userMsg);
        } catch (ApiException | NoApiKeyException | InputRequiredException | InterruptedException e) {
            System.out.println(e.getMessage());

      }
    }
}