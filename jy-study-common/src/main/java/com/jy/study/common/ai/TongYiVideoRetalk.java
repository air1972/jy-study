package com.jy.study.common.ai;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.jy.study.common.ossfile.OssClientUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

//数字人
@Component
public class TongYiVideoRetalk {
    private static final Logger log = LoggerFactory.getLogger(TongYiVideoRetalk.class);

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2video/video-synthesis/";
    private static final String TASK_STATUS_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String DEFAULT_VIDEO_URL = "https://jy-study-2025.oss-cn-shenzhen.aliyuncs.com/video/%E5%8F%A3%E5%9E%8B%E8%A7%86%E9%A2%91.mp4";

    private static final OkHttpClient client;
    
    @Value("${modelTongyi.apiKey}")
    private String apiKey;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(200, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .build();
    }

    public TongYiVideoRetalk() {
    }

    /**
     * 生成数字人视频并保存到OSS
     * @param audioUrl 音频URL
     * @return OSS中的视频URL
     */
    public String generateAndSaveVideo(String audioUrl) throws Exception {
        try {
            // 1. 提交视频生成任务
            String taskId = submitVideoRetalkTask(DEFAULT_VIDEO_URL, audioUrl, "", true);
            log.info("视频生成任务已提交，taskId: {}", taskId);

            // 2. 轮询检查任务状态，直到完成
            String videoUrl = null;
            int maxAttempts = 60;
            int intervalSeconds = 10;
            
            for (int i = 0; i < maxAttempts; i++) {
                JSONObject result = getTaskResult(taskId);
                JSONObject output = result.getJSONObject("output");
                String taskStatus = output.getString("task_status");
                
                log.info("任务状态检查 {}/{}: {}", i + 1, maxAttempts, taskStatus);
                
                if ("SUCCEEDED".equals(taskStatus)) {
                    videoUrl = output.getString("video_url");
                    break;
                } else if ("FAILED".equals(taskStatus)) {
                    throw new Exception("视频生成任务失败");
                }
                
                Thread.sleep(intervalSeconds * 1000L);
            }
            
            if (videoUrl == null) {
                throw new Exception("音频生成成功，视频生成超时，请稍后在列表中点击【重新生成】按钮重试");
            }

            // 3. 将视频从临时URL转存到OSS
            return saveVideoToOss(videoUrl);
            
        } catch (Exception e) {
            log.error("生成数字人视频失败", e);
            throw e;
        }
    }

    /**
     * 将视频从URL保存到OSS
     */
    private String saveVideoToOss(String videoUrl) throws IOException {
        // 1. 从URL下载视频
        URL url = new URL(videoUrl);
        try (InputStream videoStream = url.openStream()) {
            // 2. 生成OSS对象键
            String fileName = "retalk_" + System.currentTimeMillis() + ".mp4";
            String objectKey = "video/retalk/" + fileName;
            
            // 3. 上传到OSS
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    OssClientUtil.getBucketName(),
                    objectKey,
                    videoStream
            );
            
            OssClientUtil.getOSSClient().putObject(putObjectRequest);
            
            // 4. 生成永久访问URL
            return "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;
        }
    }

    public String submitVideoRetalkTask(String videoUrl, String audioUrl, String refImageUrl, boolean videoExtension) throws IOException {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "videoretalk");

        JSONObject input = new JSONObject();
        input.put("video_url", videoUrl);
        input.put("audio_url", audioUrl);
        input.put("ref_image_url", refImageUrl);
        requestBody.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("video_extension", videoExtension);
        requestBody.put("parameters", parameters);

        // 构建请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("X-DashScope-Async", "enable")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            System.out.println("Response: " + responseBody);

            // 解析响应获取taskId
            JSONObject jsonResponse = JSONObject.parseObject(responseBody);
            return jsonResponse.getJSONObject("output").getString("task_id");
        }
    }

    public String checkTaskStatus(String taskId) throws IOException {
        Request request = new Request.Builder()
                .url(TASK_STATUS_URL + taskId)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            System.out.println("Task status response: " + responseBody);
            return responseBody;
        }
    }

    /**
     * 获取任务结果
     * @param taskId 任务ID
     * @return 任务结果的JSON对象
     */
    public JSONObject getTaskResult(String taskId) throws IOException {
        Request request = new Request.Builder()
                .url(TASK_STATUS_URL + taskId)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            return JSONObject.parseObject(responseBody);
        }
    }

    public static void main(String[] args) {
//        String apiKey = "sk-29b";
//
//        try {
//            // 使用一个已知的taskId来测试
//            String taskId = "3f4c6c9b-fc07-4e01-baa8-e471b20408c0";
//
//            // 获取任务结果
//            JSONObject result = getTaskResult(taskId);
//            System.out.println("完整响应结果：" + result.toJSONString());
//
//            // 获取output部分
//            JSONObject output = result.getJSONObject("output");
//            if (output != null) {
//                // 获取任务状态
//                String taskStatus = output.getString("task_status");
//                System.out.println("任务状态：" + taskStatus);
//
//                // 如果任务成功，获取视频URL
//                if ("SUCCEEDED".equals(taskStatus)) {
//                    String videoUrl = output.getString("video_url");
//                    System.out.println("生成的视频URL：" + videoUrl);
//                }
//            }
//
//        } catch (IOException e) {
//            System.err.println("API请求出错：" + e.getMessage());
//            e.printStackTrace();
//        }
    }
}

