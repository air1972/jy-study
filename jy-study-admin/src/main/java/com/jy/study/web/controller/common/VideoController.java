package com.jy.study.web.controller.common;

import com.aliyun.oss.model.PutObjectRequest;
import com.jy.study.common.ai.TongYiVideoRetalk;
import com.jy.study.common.ai.TongYiVoice;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.ossfile.OssClientUtil;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyLessonService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

@Controller
@RequestMapping("/video")
public class VideoController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(VideoController.class);
    
    private static final int MAX_VIDEO_TEXT_LENGTH = 500;

    @Autowired
    private TongYiVideoRetalk videoRetalk;
    
    @Autowired
    private TongYiVoice tongYiVoice;
    
    @Autowired
    private IStudyLessonService lessonService;

    /**
     * 清理HTML内容，只保留纯文本
     */
    private String cleanHtmlContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        try {
            Document doc = Jsoup.parse(content);
            String text = doc.text();
            return text.replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            log.warn("清理HTML内容失败", e);
            return content;
        }
    }

    /**
     * 生成音频文件并保存到OSS
     */
    private String generateAndSaveVoice(String text) throws Exception {
        // 1. 生成音频
        ByteBuffer audioBuffer = tongYiVoice.generateVoice(text);
        if (audioBuffer == null) {
            throw new Exception("音频生成失败");
        }

        // 2. 上传到OSS
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp3";
        String objectKey = "voice/" + fileName;

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    OssClientUtil.getBucketName(),
                    objectKey,
                    new ByteArrayInputStream(audioBuffer.array())
            );
            OssClientUtil.getOSSClient().putObject(putObjectRequest);

            // 3. 生成永久访问URL
            return "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;
        } catch (Exception e) {
            log.error("上传音频文件到OSS失败", e);
            throw new Exception("上传音频文件失败");
        }
    }

    /**
     * 生成数字人视频
     */
    @PostMapping("/generate/retalk")
    @ResponseBody
    public AjaxResult generateRetalkVideo(Long lessonId, String content) {
        try {
            // 1. 查询课程
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if (lesson == null) {
                return AjaxResult.error("课程不存在");
            }

            // 2. 清理HTML内容
            String cleanContent = cleanHtmlContent(content);
            if (StringUtils.isEmpty(cleanContent)) {
                return AjaxResult.error("内容为空");
            }

            // 3. 生成音频URL
            String audioUrl = generateAndSaveVoice(cleanContent);
            log.info("音频生成成功，URL: {}", audioUrl);

            // 4. 根据内容长度决定是否生成视频
            String finalUrl;
            if (cleanContent.length() <= MAX_VIDEO_TEXT_LENGTH) {
                // 内容较短，生成视频
                try {
                    String videoUrl = videoRetalk.generateAndSaveVideo(audioUrl);
                    log.info("视频生成成功，URL: {}", videoUrl);
                    finalUrl = videoUrl;
                } catch (Exception e) {
                    log.error("视频生成失败，使用音频URL", e);
                    finalUrl = audioUrl;
                }
            } else {
                // 内容较长，只使用音频
                log.info("内容超过{}字符，仅生成音频", MAX_VIDEO_TEXT_LENGTH);
                finalUrl = audioUrl;
            }

            // 5. 更新数据库
            lesson.setVideoUrl(finalUrl);
            lesson.setVideoSubtitleText(cleanContent);
            lessonService.updateStudyLesson(lesson);

            return AjaxResult.success("生成成功", finalUrl);
        } catch (Exception e) {
            log.error("生成数字人视频失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }
}