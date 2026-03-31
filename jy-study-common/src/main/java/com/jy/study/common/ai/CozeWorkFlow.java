package com.jy.study.common.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Coze工作流执行类
 */
@Data
@Component
public class CozeWorkFlow {
    
    private static final Logger log = LoggerFactory.getLogger(CozeWorkFlow.class);
    private static final String API_URL = "https://api.coze.cn/v1/workflow/run";
    
    @Value("${cozeAI.token:placeholder-not-used}")
    private String token;
    
    // 添加超时时间配置
    private static final int CONNECT_TIMEOUT = 30; // 连接超时30秒
    private static final int READ_TIMEOUT = 360;    // 读取超时360秒
    private static final int WRITE_TIMEOUT = 300;   // 写入超时300秒

    /**
     * 执行工作流
     *
     * @param parameters 参数
     * @return 执行结果中的output字段
     */
    public CozeResponse runWorkflow(Map<String, Object> parameters) {
        try {
            // 构建OkHttpClient，设置超时
            OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();

            // 构建请求体
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    JSON.toJSONString(new WorkflowRequest(parameters))
            );

            // 构建请求
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            // 执行请求
            Response response = client.newCall(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                log.info("Coze API响应: {}", responseBody);
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                if (jsonResponse.getIntValue("code") == 0) {
                    CozeResponse result = new CozeResponse();
                    
                    // 解析data中的内容
                    JSONObject data = JSON.parseObject(jsonResponse.getString("data"));
                    result.setOutput(data.getString("output"));
                    result.setFileUrl(data.getString("url"));
                    
                    // 设置debug_url
                    result.setDebugUrl(jsonResponse.getString("debug_url"));
                    
                    return result;
                } else {
                    String errorMsg = jsonResponse.getString("msg");
                    log.error("Coze API返回错误: {}", errorMsg);
                    throw new RuntimeException("API调用失败: " + errorMsg);
                }
            } else {
                String errorBody = response.body() != null ? response.body().string() : "未知错误";
                log.error("执行工作流失败: {}, {}", response.code(), errorBody);
                throw new RuntimeException("API调用失败: " + response.code());
            }
        } catch (Exception e) {
            log.error("执行工作流异常", e);
            throw new RuntimeException("API调用异常: " + e.getMessage());
        }
    }

    @Data
    private static class WorkflowRequest {
        private String app_id = "7492698300899639306";
        private String workflow_id = "7492761359478767667";
        private Map<String, Object> parameters;

        public WorkflowRequest(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }

    public static void main(String[] args) {
        CozeWorkFlow cozeWorkFlow = new CozeWorkFlow();

        // 准备参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("xuanze", 10);  // 选择题数量
        parameters.put("tiankong", 5); // 填空题数量
        parameters.put("panduan", 8);  // 判断题数量
        parameters.put("jianda", 3);   // 简答题数量
        parameters.put("File", "六、亚洲——我们生活的大洲 （一）自然环境概览 地理位置：亚洲大部分位于东半球和北半球，这一地理位置赋予了它独特的自然风貌。" +
                "海洋毗邻：亚洲北接北冰洋，东临太平洋，南濒印度洋，这样的海洋分布对其气候和生态环境产生了深远的影响。" +
                "相邻大洲与分界线：亚洲与欧洲以乌拉尔山、乌拉尔河等自然特征为界，与非洲则以苏伊士运河相隔，这种地理位置的独特性使得亚洲的自然环境呈现出多样性。" +
                "面积与分区：亚洲是世界上面积最大的洲，且跨纬度最广、东西距离最长。它被划分为东亚、东南亚、南亚、西亚、中亚和北亚等地理区域，每个区域都有其独特的地貌和文化。" +
                "地形特点与主要地形区：亚洲地形复杂多样，以高原和山地为主，地面起伏大，中间高四周低。其中，青藏高原和西西伯利亚平原是亚洲最具代表性的两大地形区。" +
                "河流与湖泊：亚洲的河流多发源于中部山地和高原，呈放射状流向周边海洋。同时，里海、贝加尔湖和死海等湖泊也各具特色，分别展现了亚洲不同地区的水域风貌。" +
                "气候特点与主要气候类型：亚洲气候复杂多样，季风气候显著，大陆性气候分布广泛。这里包括热带雨林气候、热带季风气候、亚热带季风气候等多种气候类型，每种气候类型都孕育了独特的生态环境和生物多样性" +
                "（二）人文环境探究" +
                "接下来，我们将深入了解亚洲的人文环境。" +
                "截至2000年，全球总人口约为60.55亿，而亚洲人口达到了惊人的36.8亿，占据了总人口的61%，稳居各大洲之首。" +
                "全球范围内，有九个国家人口超过1亿。其中，亚洲便占据了六个席位，包括中国、印度、印度尼西亚、巴基斯坦、孟加拉国以及日本。" +
                "在各大洲中，除南极洲外，亚洲的人口数位居榜首，其次是非洲、欧洲、拉丁美洲、北美洲和大洋州。" +
                "同样地，在平均人口自然增长率方面，除南极洲外，非洲、拉丁美洲、亚洲、大洋州、北美洲和欧洲的排名依次递减。" +
                "亚洲庞大的人口数量对当地的资源和环境带来了巨大的挑战和压力。" +
                "亚洲拥有约1000个大小民族，这一数字约占全球民族总数的一半。其中，汉族因其庞大的人口数量而闻名于世。" +
                "亚洲拥有三个重要的人类文明发源地：黄河与长江中下游地区、印度河流域，以及美索不达米亚平原（即两河流域）。这些地区之所以能够孕育出灿烂的文明，得益于它们适宜的温带或热带气候、充足的水源，以及肥沃的土地。" +
                "亚洲各地区的民族在建筑风格、服饰特色、音乐舞蹈，以及礼仪习俗等方面，都展现出了丰富多彩的文化艺术风貌和独特的民族风俗。" +
                "不同地区的文化发展与当地的自然条件密切相关。" +
                "亚洲的经济发展呈现出显著的不平衡性，少数国家属于发达国家，而大多数国家仍为发展中国家。"); // 输入的课程内容

        // 执行工作流
        CozeResponse result = cozeWorkFlow.runWorkflow(parameters);

        // 打印结果
        System.out.println("工作流执行结果：");
        System.out.println(result.getOutput());

        // 如果需要，可以把结果解析成JSON对象查看具体内容
        try {
            System.out.println("解析后的JSON结果：");
            System.out.println(JSON.toJSONString(result, true)); // 格式化输出
        } catch (Exception e) {
            System.out.println("结果解析失败：" + e.getMessage());
        }
    }
}
