package com.jy.study.common.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 通义千问 AI 调用类
 */
@Data
@Component
public class TongYiAI {

    private static final Logger log = LoggerFactory.getLogger(TongYiAI.class);
    // 通义千问 API 地址
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    @Value("${modelTongyi.apiKey}")
    private String apiKey;

    // 模型名称
    private static final String MODEL = "qwen-plus";
    
    // 超时时间配置
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 360;
    private static final int WRITE_TIMEOUT = 300;

    /**
     * 生成试题
     *
     * @param xuanze 选择题数量
     * @param tiankong 填空题数量
     * @param panduan 判断题数量
     * @param jianda 简答题数量
     * @param content 文章内容
     * @return 生成的试题内容
     */
    public String generateQuestions(Integer xuanze, Integer tiankong, Integer panduan, 
                                    Integer jianda, String content) {
        String prompt = buildPrompt(xuanze, tiankong, panduan, jianda, content);
        return callTongYiAI(prompt);
    }

    /**
     * 构建 Prompt
     */
    private String buildPrompt(Integer xuanze, Integer tiankong, Integer panduan, 
                               Integer jianda, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的教育出题专家。请根据以下文章内容，生成一套标准的中国试卷格式试题。\n\n");
        sb.append("【文章内容】\n");
        sb.append(content);
        sb.append("\n\n");
        sb.append("【出题要求】\n");
        int sectionIndex = 1;
        if (xuanze > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、选择题（每题 4 个选项，只有一个正确答案）\n");
        }
        if (tiankong > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、填空题\n");
        }
        if (panduan > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、判断题\n");
        }
        if (jianda > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、简答题\n");
        }
        sb.append("\n【输出格式要求】\n");
        sb.append("请严格按照以下格式输出，不要使用 markdown 代码块包裹，不要有任何多余的文字：\n\n");
        
        sectionIndex = 1;
        if (xuanze > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、选择题\n");
            sb.append("1、题目内容\n");
            sb.append("A. 选项A\n");
            sb.append("B. 选项B\n");
            sb.append("C. 选项C\n");
            sb.append("D. 选项D\n");
            sb.append("[正确答案] A\n");
            sb.append("[解析] 解析内容\n\n");
        }
        
        if (tiankong > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、填空题\n");
            sb.append("1、题目内容（空格用____表示）\n");
            sb.append("[答案] 答案内容\n");
            sb.append("[解析] 解析内容\n\n");
        }

        if (panduan > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、判断题\n");
            sb.append("1、题目内容\n");
            sb.append("[答案] 正确/错误\n");
            sb.append("[解析] 解析内容\n\n");
        }

        if (jianda > 0) {
            sb.append(getChineseNumber(sectionIndex++)).append("、简答题\n");
            sb.append("1、题目内容\n");
            sb.append("[答案] 参考答案内容\n");
            sb.append("[解析] 解析内容\n\n");
        }

        sb.append("注意：\n");
        sb.append("1. 题目序号请使用 1、2、3、... 格式。\n");
        sb.append("2. 题型标题请使用 一、二、三、... 格式。\n");
        sb.append("3. 答案必须紧跟在题目或选项后面，使用 [正确答案] 或 [答案] 标记。\n");
        sb.append("4. 每个题目后面必须提供 [解析]。\n");
        sb.append("5. 请直接输出文本内容，不要使用 markdown 代码块包裹。");
        
        return sb.toString();
    }

    public String generateKnowledgePoints(String content) {
        String prompt = "你是一位专业的教育专家，请从以下内容中提取核心知识点，并以JSON格式返回。JSON格式为：[{\"name\": \"知识点名称\", \"description\": \"知识点描述\"}, ...]。\n\n内容：\n" + content;
        return callTongYiAI(prompt);
    }

    /**
     * 根据知识点生成题目
     */
    public String generateExerciseByKnowledge(String name, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的出题专家。请根据以下知识点生成 3 道题目（包含 1 道选择题，1 道判断题，1 道填空题）。\n\n");
        sb.append("【知识点名称】").append(name).append("\n");
        sb.append("【知识点描述】").append(description).append("\n\n");
        sb.append("【输出格式要求】\n");
        sb.append("请严格以 JSON 数组格式返回，不要有任何多余文字或 Markdown 标记。格式如下：\n");
        sb.append("[\n");
        sb.append("  {\n");
        sb.append("    \"type\": \"1\", \n");
        sb.append("    \"content\": \"题目内容\",\n");
        sb.append("    \"options\": \"{\\\"A\\\":\\\"选项A\\\",\\\"B\\\":\\\"选项B\\\",\\\"C\\\":\\\"选项C\\\",\\\"D\\\":\\\"选项D\\\"}\",\n");
        sb.append("    \"answer\": \"A\",\n");
        sb.append("    \"explanation\": \"解析内容\"\n");
        sb.append("  },\n");
        sb.append("  {\n");
        sb.append("    \"type\": \"3\", \n");
        sb.append("    \"content\": \"题目内容\",\n");
        sb.append("    \"options\": null,\n");
        sb.append("    \"answer\": \"正确\",\n");
        sb.append("    \"explanation\": \"解析内容\"\n");
        sb.append("  },\n");
        sb.append("  {\n");
        sb.append("    \"type\": \"2\", \n");
        sb.append("    \"content\": \"题目内容（空格用____表示）\",\n");
        sb.append("    \"options\": null,\n");
        sb.append("    \"answer\": \"答案内容\",\n");
        sb.append("    \"explanation\": \"解析内容\"\n");
        sb.append("  }\n");
        sb.append("]\n");
        sb.append("注意：选择题 type 为 1，填空题 type 为 2，判断题 type 为 3。");
        
        return callTongYiAI(sb.toString());
    }

    public String generateWrongAnswerSuggestion(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位严谨的学习教练。请基于用户的错题本数据，生成一份可执行的学习建议。\n");
        sb.append("要求：\n");
        sb.append("1) 输出为纯文本，不要 Markdown，不要代码块。\n");
        sb.append("2) 分 4 段：现状诊断、优先复习知识点、建议练习计划（含每天/每次做题数量）、注意事项。\n");
        sb.append("3) 建议尽量具体，避免空泛。\n");
        sb.append("4) 字数控制在 300~500 字。\n\n");
        sb.append("【错题本数据】\n");
        sb.append(context);
        return callTongYiAI(sb.toString());
    }

    /**
     * 生成个性化学习路径（结构化 JSON）
     */
    public String generatePersonalizedLearningPath(String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业学习规划师，请基于用户学习画像生成个性化学习路线。\n");
        sb.append("输出要求：\n");
        sb.append("1) 只输出 JSON 对象，不要 Markdown、不要代码块、不要额外说明。\n");
        sb.append("2) JSON 结构必须为：\n");
        sb.append("{\"estimatedDays\":14,\"pathSteps\":[{\"stepIndex\":1,\"type\":\"lesson\",\"id\":1,\"title\":\"标题\",\"reason\":\"原因\",\"status\":\"pending\"}],\"recommendations\":[{\"type\":\"lesson\",\"id\":1,\"title\":\"标题\",\"reason\":\"原因\",\"priority\":\"high\"}]}\n");
        sb.append("3) pathSteps 最少 3 步，最多 8 步；recommendations 最少 3 条，最多 10 条。\n");
        sb.append("4) priority 只能是 high 或 medium；status 固定为 pending。\n");
        sb.append("5) 内容必须结合用户薄弱点与历史表现，避免空泛。\n\n");
        sb.append("【用户学习画像】\n");
        sb.append(context);
        return callTongYiAI(sb.toString());
    }

    private String callTongYiAI(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

            JSONObject inputParams = new JSONObject();
            inputParams.put("result_format", "message");
            
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            JSONObject input = new JSONObject();
            input.put("messages", messages);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);
            requestBody.put("input", input);
            requestBody.put("parameters", inputParams);

            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toJSONString()
            );

            Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                log.info("通义 API 响应：{}", responseBody);

                JSONObject jsonResponse = JSON.parseObject(responseBody);
                JSONObject output = jsonResponse.getJSONObject("output");
                if (output != null) {
                    JSONArray choices = output.getJSONArray("choices");
                    if (choices != null && choices.size() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");
                        if (message != null) {
                            return message.getString("content");
                        }
                    }
                }
                
                if (jsonResponse.containsKey("code")) {
                    String errorMsg = jsonResponse.getString("message");
                    log.error("通义 API 返回错误：{}", errorMsg);
                    throw new RuntimeException("API 调用失败：" + errorMsg);
                }
            }
            
            String errorBody = response.body() != null ? response.body().string() : "未知错误";
            log.error("调用通义 API 失败：{}, {}", response.code(), errorBody);
            throw new RuntimeException("API 调用失败：" + response.code());
            
        } catch (Exception e) {
            log.error("调用通义 API 异常", e);
            throw new RuntimeException("API 调用异常：" + e.getMessage());
        }
    }

    private String getChineseNumber(int number) {
        String[] chineseNumbers = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (number >= 0 && number < chineseNumbers.length) {
            return chineseNumbers[number];
        }
        return String.valueOf(number);
    }
}
