package com.jy.study.common.ai;

import com.alibaba.dashscope.audio.asr.transcription.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class TongYiVoiceToText {
    private static final Logger log = LoggerFactory.getLogger(TongYiVoiceToText.class);
    
    @Value("${modelTongyi.apiKey}")
    private String apiKey;

    /**
     * 将视频URL转换为文字
     * @param videoUrl 视频URL
     * @return 识别的文字内容
     */
    public String videoToText(String videoUrl) {
        try {
            // 创建转写请求参数
            TranscriptionParam param = TranscriptionParam.builder()
                    .apiKey(apiKey)
                    .model("paraformer-v2")
                    .parameter("language_hints", new String[]{"zh", "en"})
                    .fileUrls(Arrays.asList(videoUrl))
                    .build();

            // 提交转写请求
            Transcription transcription = new Transcription();
            TranscriptionResult result = transcription.asyncCall(param);
            
            // 等待任务完成并获取结果
            result = transcription.wait(
                    TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId()));

            // 解析转写结果
            StringBuilder textBuilder = new StringBuilder();
            List<TranscriptionTaskResult> taskResultList = result.getResults();
            if (taskResultList != null && !taskResultList.isEmpty()) {
                for (TranscriptionTaskResult taskResult : taskResultList) {
                    String transcriptionUrl = taskResult.getTranscriptionUrl();
                    HttpURLConnection connection =
                            (HttpURLConnection) new URL(transcriptionUrl).openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    try (InputStream inputStream = connection.getInputStream()) {
                        byte[] body = toByteArray(inputStream);
                        String rawJson = decodeJsonBody(body, connection.getContentType());
                        JsonObject jsonResult = new GsonBuilder().create()
                                .fromJson(rawJson, JsonObject.class);

                        // 从JSON中提取文本内容
                        if (jsonResult != null && jsonResult.has("transcripts")) {
                            JsonArray transcripts = jsonResult.getAsJsonArray("transcripts");
                            for (JsonElement transcript : transcripts) {
                                JsonObject transcriptObj = transcript.getAsJsonObject();
                                if (transcriptObj.has("sentences")) {
                                    JsonArray sentences = transcriptObj.getAsJsonArray("sentences");
                                    for (JsonElement sentence : sentences) {
                                        JsonObject sentenceObj = sentence.getAsJsonObject();
                                        if (sentenceObj.has("text")) {
                                            String sentenceText = sentenceObj.get("text").getAsString();
                                            Long start = extractTimeMs(sentenceObj, "begin_time", "beginTime", "start_time", "startTime");
                                            Long end = extractTimeMs(sentenceObj, "end_time", "endTime", "stop_time", "stopTime");
                                            if (start != null && end != null && end > start) {
                                                textBuilder.append("[[").append(start).append(",").append(end).append("]]");
                                            }
                                            textBuilder.append(sentenceText).append("\n");
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        connection.disconnect();
                    }
                }
            }

            String textResult = normalizeSubtitleText(textBuilder.toString());
            if (textResult.isEmpty()) {
                throw new RuntimeException("未能识别出任何文字");
            }
            return textResult;
            
        } catch (Exception e) {
            log.error("视频转文字失败", e);
            throw new RuntimeException("视频转文字失败: " + e.getMessage());
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    private String decodeJsonBody(byte[] body, String contentType) {
        Charset headerCharset = extractCharset(contentType);
        if (headerCharset != null) {
            return new String(body, headerCharset);
        }

        String utf8Text = new String(body, StandardCharsets.UTF_8);
        if (utf8Text.contains("\uFFFD")) {
            String gb18030Text = new String(body, Charset.forName("GB18030"));
            return readabilityScore(gb18030Text) > readabilityScore(utf8Text) ? gb18030Text : utf8Text;
        }
        return utf8Text;
    }

    private Charset extractCharset(String contentType) {
        if (contentType == null) {
            return null;
        }
        String lower = contentType.toLowerCase();
        int idx = lower.indexOf("charset=");
        if (idx < 0) {
            return null;
        }
        String charsetName = lower.substring(idx + 8).trim();
        int semicolon = charsetName.indexOf(';');
        if (semicolon >= 0) {
            charsetName = charsetName.substring(0, semicolon).trim();
        }
        if (charsetName.startsWith("\"") && charsetName.endsWith("\"") && charsetName.length() > 1) {
            charsetName = charsetName.substring(1, charsetName.length() - 1);
        }
        try {
            return Charset.forName(charsetName);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeSubtitleText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text
                .replace("\u0000", "")
                .replaceAll("[\\u0001-\\u0008\\u000B\\u000C\\u000E-\\u001F]", "")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }

        String best = cleaned;
        int bestScore = readabilityScore(cleaned);

        String[] candidates = new String[] {
                tryReDecode(cleaned, "ISO-8859-1", "UTF-8"),
                tryReDecode(cleaned, "GBK", "UTF-8"),
                tryReDecode(cleaned, "GB18030", "UTF-8"),
                tryReDecode(cleaned, "UTF-8", "GBK")
        };
        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            int score = readabilityScore(candidate);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }

        return best.replaceAll("\\n{3,}", "\n\n").trim();
    }

    private String tryReDecode(String source, String fromCharset, String toCharset) {
        try {
            return new String(source.getBytes(fromCharset), toCharset);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int readabilityScore(String text) {
        if (text == null || text.isEmpty()) {
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
            } else if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                score -= 3;
            }
            if (common.indexOf(c) >= 0) {
                score += 2;
            }
            if (mojibake.indexOf(c) >= 0) {
                score -= 3;
            }
            if (c == '\u951F' || c == '\uFFFD') {
                score -= 2;
            }
        }
        return score;
    }

    private Long extractTimeMs(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (!obj.has(key) || obj.get(key).isJsonNull()) {
                continue;
            }
            try {
                JsonElement element = obj.get(key);
                if (element.getAsJsonPrimitive().isNumber()) {
                    double value = element.getAsDouble();
                    return value < 100000 ? (long) (value * 1000) : (long) value;
                }
                String raw = element.getAsString();
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                double value = Double.parseDouble(raw.trim());
                return value < 100000 ? (long) (value * 1000) : (long) value;
            } catch (Exception ignore) {
            }
        }
        return null;
    }
}
