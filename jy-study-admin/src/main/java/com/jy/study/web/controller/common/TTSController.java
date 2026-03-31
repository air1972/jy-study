package com.jy.study.web.controller.common;

import com.jy.study.common.ai.TongYiVoice;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.common.ossfile.OssClientUtil;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.service.IStudyArticleService;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyLessonService;
import com.jy.study.common.ai.TongYiVoiceToText;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/tts")
public class TTSController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TTSController.class);
    
    private static final int MAX_TEXT_LENGTH = 10000;

    @Autowired
    private TongYiVoice tongYiVoice;

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyLessonService lessonService;

    @Autowired
    private TongYiVoiceToText voiceToText;

    private static final Map<Long, SubtitleTaskStatus> SUBTITLE_TASKS = new ConcurrentHashMap<>();

    /**
     * 清理HTML内容，只保留纯文本
     */
    private String cleanHtmlContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return "";
        }
        try {
            // 解析HTML
            Document doc = Jsoup.parse(content);
            // 获取纯文本内容
            String text = doc.text();
            // 移除多余的空白字符
            text = text.replaceAll("\\s+", " ").trim();
            return text;
        } catch (Exception e) {
            log.warn("清理HTML内容失败", e);
            // 如果解析失败，返回原内容
            return content;
        }
    }

    @PostMapping("/generate/voice")
    @ResponseBody
    public AjaxResult generateVoice(Long articleId) {
        try {
            // 1. 查询文章
            StudyArticle article = articleService.selectArticleById(articleId);
            if (article == null) {
                return AjaxResult.error("文章不存在");
            }

            // 2. 检查是否已有语音URL
            if (StringUtils.isNotEmpty(article.getVoiceUrl())) {
                return AjaxResult.success("语音已存在", article.getVoiceUrl());
            }

            // 3. 准备文本内容，清理HTML标签
            String cleanTitle = cleanHtmlContent(article.getTitle());
            String cleanContent = cleanHtmlContent(article.getContent());
            String text = cleanTitle + "。" + cleanContent;
            
            // 截取文本以符合API限制
            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            // 4. 生成语音
            ByteBuffer audioBuffer = tongYiVoice.generateVoice(text);
            if (audioBuffer == null) {
                return AjaxResult.error("语音生成失败");
            }

            // 5. 上传到OSS
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".mp3";
            String objectKey = "voice/" + fileName;

            OSS ossClient = OssClientUtil.getOSSClient();
            try {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        OssClientUtil.getBucketName(),
                        objectKey,
                        new ByteArrayInputStream(audioBuffer.array())
                );
                ossClient.putObject(putObjectRequest);

                // 6. 生成永久访问URL
                String voiceUrl = "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;

                // 7. 更新文章的voice_url
                article.setVoiceUrl(voiceUrl);
                articleService.updateArticle(article);

                return AjaxResult.success("生成成功", voiceUrl);
            } catch (Exception e) {
                log.error("上传语音文件到OSS失败", e);
                return AjaxResult.error("上传语音文件失败");
            }
        } catch (Exception e) {
            log.error("生成语音失败", e);
            return AjaxResult.error("生成语音失败：" + e.getMessage());
        }
    }

    /**
     * 识别视频字幕
     */
    @PostMapping("/recognize/subtitle")
    @ResponseBody
    public AjaxResult recognizeSubtitle(Long lessonId, Boolean force) {
        try {
            // 1. 查询课程
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if (lesson == null) {
                return AjaxResult.error("课程不存在");
            }

            // 2. 检查是否已有字幕文本
            if (!Boolean.TRUE.equals(force) && StringUtils.isNotEmpty(lesson.getVideoSubtitleText())) {
                String subtitle = normalizeExistingSubtitle(lesson);
                return AjaxResult.success("字幕已存在", subtitle);
            }

            // 3. 检查是否有视频URL
            if (StringUtils.isEmpty(lesson.getVideoUrl())) {
                return AjaxResult.error("课程没有视频");
            }

            // 4. 调用识别服务
            String subtitleText = voiceToText.videoToText(lesson.getVideoUrl());
            if (StringUtils.isEmpty(subtitleText)) {
                return AjaxResult.error("字幕识别失败");
            }

            // 5. 更新数据库
            lesson.setVideoSubtitleText(subtitleText);
            lessonService.updateStudyLesson(lesson);

            return AjaxResult.success("识别成功", subtitleText);
        } catch (Exception e) {
            log.error("识别字幕失败", e);
            return AjaxResult.error("识别字幕失败：" + e.getMessage());
        }
    }

    /**
     * 异步开始识别字幕（长视频推荐）
     */
    @PostMapping("/recognize/subtitle/start")
    @ResponseBody
    public AjaxResult startRecognizeSubtitle(Long lessonId, Boolean force) {
        try {
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if (lesson == null) {
                return AjaxResult.error("课程不存在");
            }
            if (StringUtils.isEmpty(lesson.getVideoUrl())) {
                return AjaxResult.error("课程没有视频");
            }

            // 已经有字幕，直接返回完成态
            if (!Boolean.TRUE.equals(force) && StringUtils.isNotEmpty(lesson.getVideoSubtitleText())) {
                String subtitle = normalizeExistingSubtitle(lesson);
                SubtitleTaskStatus done = new SubtitleTaskStatus("success", "字幕已存在");
                done.setResult(subtitle);
                done.setUpdateTime(new Date());
                SUBTITLE_TASKS.put(lessonId, done);
                return AjaxResult.success("字幕已存在", done);
            }

            SubtitleTaskStatus current = SUBTITLE_TASKS.get(lessonId);
            if (current != null && "running".equals(current.getStatus())) {
                return AjaxResult.success("字幕识别任务进行中", current);
            }

            SubtitleTaskStatus running = new SubtitleTaskStatus("running", "字幕识别中，请稍候...");
            running.setProgress(0);
            running.setUpdateTime(new Date());
            SUBTITLE_TASKS.put(lessonId, running);

            new Thread(() -> {
                try {
                    String subtitleText = recognizeSubtitleBySegments(lessonId, lesson.getVideoUrl());
                    if (StringUtils.isEmpty(subtitleText)) {
                        SubtitleTaskStatus failed = new SubtitleTaskStatus("failed", "字幕识别失败");
                        failed.setUpdateTime(new Date());
                        SUBTITLE_TASKS.put(lessonId, failed);
                        return;
                    }

                    StudyLesson updateLesson = new StudyLesson();
                    updateLesson.setLessonId(lessonId);
                    updateLesson.setVideoSubtitleText(subtitleText);
                    lessonService.updateStudyLesson(updateLesson);

                    SubtitleTaskStatus success = new SubtitleTaskStatus("success", "字幕识别完成");
                    success.setResult(subtitleText);
                    success.setProgress(100);
                    success.setUpdateTime(new Date());
                    SUBTITLE_TASKS.put(lessonId, success);
                } catch (Exception ex) {
                    log.error("异步识别字幕失败，lessonId={}", lessonId, ex);
                    SubtitleTaskStatus failed = new SubtitleTaskStatus("failed", "字幕识别失败：" + ex.getMessage());
                    failed.setUpdateTime(new Date());
                    SUBTITLE_TASKS.put(lessonId, failed);
                }
            }, "subtitle-task-" + lessonId).start();

            return AjaxResult.success("已开始识别字幕", running);
        } catch (Exception e) {
            log.error("启动字幕识别任务失败", e);
            return AjaxResult.error("启动失败：" + e.getMessage());
        }
    }

    private String recognizeSubtitleBySegments(Long lessonId, String videoUrl) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("subtitle-" + lessonId + "-");
            boolean ffmpegReady = checkFfmpegAvailable();
            if (!ffmpegReady) {
                updateTaskStatus(lessonId, "running", "未检测到 ffmpeg，降级为整段识别中...", 10, null);
                return voiceToText.videoToText(videoUrl);
            }

            updateTaskStatus(lessonId, "running", "正在切分视频片段...", 5, null);
            List<File> segments = splitVideoByFfmpeg(videoUrl, tempDir);
            if (segments.isEmpty()) {
                updateTaskStatus(lessonId, "running", "未切分到视频片段，降级为整段识别中...", 10, null);
                return voiceToText.videoToText(videoUrl);
            }

            StringBuilder merged = new StringBuilder();
            int total = segments.size();
            for (int i = 0; i < total; i++) {
                int index = i + 1;
                File segment = segments.get(i);
                updateTaskStatus(lessonId, "running", "正在识别第 " + index + "/" + total + " 段...", calcProgress(index, total), merged.toString());

                String segmentUrl = uploadSegmentToOss(segment);
                String chunkText = voiceToText.videoToText(segmentUrl);
                if (StringUtils.isNotEmpty(chunkText)) {
                    if (merged.length() > 0) {
                        merged.append("\n");
                    }
                    merged.append(chunkText.trim());
                }

                updateTaskStatus(lessonId, "running", "已完成 " + index + "/" + total + " 段识别", calcProgress(index, total), merged.toString());
            }
            return merged.toString().trim();
        } catch (Exception e) {
            log.error("分段识别失败，降级整段识别。lessonId={}", lessonId, e);
            updateTaskStatus(lessonId, "running", "分段识别失败，降级为整段识别中...", 15, null);
            return voiceToText.videoToText(videoUrl);
        } finally {
            deleteDirectoryQuietly(tempDir);
        }
    }

    private boolean checkFfmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").redirectErrorStream(true).start();
            int code = process.waitFor();
            return code == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private List<File> splitVideoByFfmpeg(String videoUrl, Path tempDir) throws IOException, InterruptedException {
        List<File> files = new ArrayList<>();
        String outputPattern = tempDir.resolve("chunk_%03d.mp4").toString();
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", videoUrl,
                "-map", "0",
                "-c", "copy",
                "-f", "segment",
                "-segment_time", "120",
                "-reset_timestamps", "1",
                outputPattern
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("ffmpeg 切分失败，退出码：" + exitCode);
        }

        Files.list(tempDir)
                .filter(p -> p.getFileName().toString().startsWith("chunk_") && p.getFileName().toString().endsWith(".mp4"))
                .sorted(Comparator.comparing(Path::toString))
                .forEach(p -> files.add(p.toFile()));
        return files;
    }

    private String uploadSegmentToOss(File segment) {
        String folder = "subtitle-segments/" + UUID.randomUUID().toString().replaceAll("-", "");
        String objectKey = folder + "/" + segment.getName();
        OSS ossClient = OssClientUtil.getOSSClient();
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    OssClientUtil.getBucketName(),
                    objectKey,
                    segment
            );
            ossClient.putObject(putObjectRequest);
            return "https://" + OssClientUtil.getBucketName() + "." + OssClientUtil.getEndpoint() + "/" + objectKey;
        } finally {
            ossClient.shutdown();
        }
    }

    private int calcProgress(int current, int total) {
        if (total <= 0) return 0;
        int progress = (int) Math.round((current * 90.0) / total) + 10;
        return Math.min(progress, 99);
    }

    private void updateTaskStatus(Long lessonId, String status, String message, Integer progress, String partialResult) {
        SubtitleTaskStatus task = SUBTITLE_TASKS.getOrDefault(lessonId, new SubtitleTaskStatus());
        task.setStatus(status);
        task.setMessage(message);
        task.setProgress(progress);
        task.setPartialResult(partialResult);
        task.setUpdateTime(new Date());
        SUBTITLE_TASKS.put(lessonId, task);
    }

    private void deleteDirectoryQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignore) {
                        }
                    });
        } catch (IOException ignore) {
        }
    }

    /**
     * 查询异步字幕识别状态
     */
    @GetMapping("/recognize/subtitle/status")
    @ResponseBody
    public AjaxResult subtitleStatus(Long lessonId) {
        try {
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if (lesson == null) {
                return AjaxResult.error("课程不存在");
            }

            SubtitleTaskStatus status = SUBTITLE_TASKS.get(lessonId);
            if (status != null && "running".equals(status.getStatus())) {
                return AjaxResult.success(status);
            }

            if (StringUtils.isNotEmpty(lesson.getVideoSubtitleText())) {
                String subtitle = normalizeExistingSubtitle(lesson);
                SubtitleTaskStatus done = new SubtitleTaskStatus("success", "字幕识别完成");
                done.setResult(subtitle);
                done.setUpdateTime(new Date());
                SUBTITLE_TASKS.put(lessonId, done);
                return AjaxResult.success(done);
            }

            if (status == null) {
                status = new SubtitleTaskStatus("idle", "尚未开始识别");
                status.setUpdateTime(new Date());
            }
            return AjaxResult.success(status);
        } catch (Exception e) {
            log.error("查询字幕识别状态失败", e);
            return AjaxResult.error("查询状态失败：" + e.getMessage());
        }
    }

    private String normalizeExistingSubtitle(StudyLesson lesson) {
        String source = lesson.getVideoSubtitleText();
        String normalized = normalizeLegacySubtitle(source);
        if (StringUtils.isNotEmpty(normalized) && !StringUtils.equals(source, normalized)) {
            StudyLesson updateLesson = new StudyLesson();
            updateLesson.setLessonId(lesson.getLessonId());
            updateLesson.setVideoSubtitleText(normalized);
            lessonService.updateStudyLesson(updateLesson);
            lesson.setVideoSubtitleText(normalized);
        }
        return lesson.getVideoSubtitleText();
    }

    private String normalizeLegacySubtitle(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        String source = text.trim();
        String best = source;
        int bestScore = readabilityScore(source);

        String[] candidates = new String[] {
                tryReDecode(source, "ISO-8859-1", "UTF-8"),
                tryReDecode(source, "GBK", "UTF-8"),
                tryReDecode(source, "GB18030", "UTF-8"),
                tryReDecode(source, "UTF-8", "GBK")
        };
        for (String candidate : candidates) {
            if (StringUtils.isEmpty(candidate)) {
                continue;
            }
            int score = readabilityScore(candidate);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private String tryReDecode(String text, String fromCharset, String toCharset) {
        try {
            return new String(text.getBytes(fromCharset), toCharset);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int readabilityScore(String text) {
        if (StringUtils.isEmpty(text)) {
            return Integer.MIN_VALUE / 2;
        }
        final String common = "的一是在不了有人和中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小现实加量都两体制机当使点从业本去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老基资边流路级少图山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处理世府车群则门即保治北造百规热领七海口东导器压志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完类离华名确才科张信马节话米整空元况今集温传土许步近广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严龙飞";
        final String mojibake = "鍙鍚鍥鍓鍔鍙鑰鎴鎵鐨鏄鏈鍦涓璇璧杩杩閫閮闂閿鎯鎶鎵鏁鏃鏉鏋缁缈缁娴濂鍥瀵氬垎锛銆鈥锟";
        int score = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '\u4E00' && c <= '\u9FFF') {
                score += 2;
            } else if (Character.isLetterOrDigit(c)) {
                score += 1;
            } else if (c == '\uFFFD') {
                score -= 6;
            }
            if (common.indexOf(c) >= 0) {
                score += 2;
            }
            if (mojibake.indexOf(c) >= 0) {
                score -= 3;
            }
        }
        return score;
    }

    public static class SubtitleTaskStatus {
        private String status;
        private String message;
        private String result;
        private String partialResult;
        private Integer progress;
        private Date updateTime;

        public SubtitleTaskStatus() {}

        public SubtitleTaskStatus(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public String getPartialResult() {
            return partialResult;
        }

        public void setPartialResult(String partialResult) {
            this.partialResult = partialResult;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }
    }
}
