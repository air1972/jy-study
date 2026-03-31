package com.jy.study.common.ai;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TongYiVoice {
    
    @Value("${modelTongyi.apiKey}")
    private String apiKey;
    
    private static final String MODEL = "cosyvoice-v1";
    private static final String VOICE = "longxiaochun";

    public ByteBuffer generateVoice(String text) {
        try {
            SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                    .apiKey(apiKey)
                    .model(MODEL)
                    .voice(VOICE)
                    .build();
                    
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
            return synthesizer.call(text);
        } catch (Exception e) {
            throw new RuntimeException("语音生成失败: " + e.getMessage());
        }
    }
}