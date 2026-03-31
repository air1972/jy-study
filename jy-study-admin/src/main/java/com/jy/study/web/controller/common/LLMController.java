package com.jy.study.web.controller.common;

import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.ResultCallback;
import com.jy.study.common.ai.SiliconCloudAI;
import com.jy.study.common.ai.TongYiMultiRound;
import com.jy.study.common.core.controller.BaseController;
import com.jy.study.common.core.domain.AjaxResult;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.service.IStudyArticleService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.dashscope.common.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.springframework.util.StringUtils;
import com.jy.study.lesson.domain.StudyAiChat;
import com.jy.study.lesson.service.IStudyAiChatService;
import com.alibaba.dashscope.aigc.generation.Generation;
import java.util.Date;
import com.alibaba.dashscope.common.Role;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import com.jy.study.lesson.domain.StudyLesson;
import com.jy.study.lesson.service.IStudyLessonService;

@Controller
@RequestMapping("/llm")
public class LLMController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(LLMController.class);

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private SiliconCloudAI siliconCloudAI;

    @Autowired
    private TongYiMultiRound tongYiMultiRound;

    @Autowired
    private IStudyAiChatService studyAiChatService;

    @Autowired
    private IStudyLessonService lessonService;

    public static Map<Long,List<String>> cacheData = new ConcurrentHashMap<>();

    private Map<String, List<Message>> conversations = new ConcurrentHashMap<>();

    @GetMapping("/chat/stream")
    public SseEmitter chatStream(String message, String conversationId) {
        SseEmitter emitter = new SseEmitter(300000L);
        
        try {
            final Long userId = getSysUser().getUserId();
            if (userId == null) {
                emitter.send(SseEmitter.event().data("请先登录后再使用此功能").build());
                emitter.complete();
                return emitter;
            }

            final String finalConversationId = StringUtils.isEmpty(conversationId) ? 
                UUID.randomUUID().toString() : conversationId;
            
            // 获取或创建会话历史
            List<Message> messages = conversations.computeIfAbsent(finalConversationId,
                k -> Collections.synchronizedList(new ArrayList<>())
            );
            
            // 添加用户消息
            Message userMessage = tongYiMultiRound.createUserMessage(message);
            messages.add(userMessage);
            
            // 保存用户消息
            StudyAiChat userChat = new StudyAiChat();
            userChat.setConversationId(finalConversationId);
            userChat.setUserId(userId);
            userChat.setRole("user");
            userChat.setContent(message);
            userChat.setModel(Generation.Models.QWEN_PLUS);
            userChat.setStatus("0");
            userChat.setCreateTime(new Date());
            studyAiChatService.insertStudyAiChat(userChat);

            // 创建生成参数
            GenerationParam param = tongYiMultiRound.createStreamGenerationParam(messages);
            
            // 异步处理流式响应
            new Thread(() -> {
                try {
                    Semaphore semaphore = new Semaphore(0);
                    StringBuilder fullContent = new StringBuilder();
                    Long cacheId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                    List<String> contentList = new ArrayList<>();
                    
                    tongYiMultiRound.streamCall(param, new ResultCallback<GenerationResult>() {
                        @Override
                        public void onEvent(GenerationResult message) {
                            try {
                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                fullContent.append(content);
                                contentList.add(content);
                                emitter.send(content);
                            } catch (IOException e) {
                                log.error("发送流式消息失败", e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            log.error("流式对话出错", e);
                            semaphore.release();
                        }

                        @Override
                        public void onComplete() {
                            try {
                                cacheData.put(cacheId, contentList);
                                
                                Message assistantMessage = tongYiMultiRound.createAssistantMessage(fullContent.toString());
                                messages.add(assistantMessage);
                                
                                // 保存助手回复到数据库
                                StudyAiChat assistantChat = new StudyAiChat();
                                assistantChat.setConversationId(finalConversationId);
                                assistantChat.setUserId(userId);
                                assistantChat.setRole("assistant");
                                assistantChat.setContent(fullContent.toString());
                                assistantChat.setModel(Generation.Models.QWEN_PLUS);
                                assistantChat.setStatus("0");
                                assistantChat.setCreateTime(new Date());
                                studyAiChatService.insertStudyAiChat(assistantChat);
                                
                                // 发送缓存ID事件
                                emitter.send("event: cache-id\ndata: " + cacheId + "\n\n");
                                
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("完成流式对话失败", e);
                            }
                            semaphore.release();
                        }
                    });
                    
                    semaphore.acquire();
                    
                } catch (Exception e) {
                    log.error("处理流式对话失败", e);
                    try {
                        emitter.send(SseEmitter.event().data("处理失败，请稍后重试").build());
                        emitter.complete();
                    } catch (IOException ex) {
                        log.error("发送错误消息失败", ex);
                    }
                }
            }).start();
            
        } catch (Exception e) {
            log.error("创建流式对话失败", e);
            try {
                emitter.send(SseEmitter.event().data("系统错误").build());
                emitter.complete();
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
        }
        
        return emitter;
    }

    @GetMapping("/markdownToHtml2")
    @ResponseBody
    public String markdownToHtml2(Long id) {
        List<String> list = cacheData.get(id);
        if(list == null) {
            return "";
        }
        
        // 直接拼接所有内容,不需要检查DONE
        StringBuilder markdown = new StringBuilder();
        for(String str : list) {
            markdown.append(str);
        }
        cacheData.remove(id);
        
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown.toString());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    @GetMapping("/markdownToHtml")
    @ResponseBody
    public String markdownToHtml(Long id) {
        List<String> list = cacheData.get(id);
        if(list == null) {
            return "";
        }
        
        StringBuilder markdown = new StringBuilder();
        for(String str : list) {
            if(!"DONE".equals(str)) {
                markdown.append(str);
            }
        }
        cacheData.remove(id);
        
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown.toString());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    @PostMapping("/chat")
    @ResponseBody
    public AjaxResult chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        String conversationId = params.get("conversationId");
        
        try {
            // 获取或创建会话历史
            List<Message> messages = conversations.computeIfAbsent(
                conversationId == null ? UUID.randomUUID().toString() : conversationId,
                k -> {
                    List<Message> newMessages = new ArrayList<>();
                    newMessages.add(tongYiMultiRound.createSystemMessage());
                    return newMessages;
                }
            );
            
            // 添加用户消息
            messages.add(tongYiMultiRound.createUserMessage(message));
                
            // 创建生成参数
            GenerationParam param = tongYiMultiRound.createGenerationParam(messages);
            
            // 调用模型获取响应
            GenerationResult result = tongYiMultiRound.callGenerationWithMessages(param);
            
            if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null 
                || result.getOutput().getChoices().isEmpty()) {
                return AjaxResult.error("AI响应异常");
            }
            
            // 获取助手回复
            Message assistantMessage = result.getOutput().getChoices().get(0).getMessage();
            messages.add(assistantMessage);
            
            // 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("content", assistantMessage.getContent());
            data.put("conversationId", conversationId == null ? UUID.randomUUID().toString() : conversationId);
            
            return AjaxResult.success(data);
            
        } catch (Exception e) {
            log.error("AI对话出错", e);
            return AjaxResult.error("AI对话服务出现错误: " + e.getMessage());
        }
    }

    @GetMapping("/article/chat/{articleId}")
    public SseEmitter articleChatStream(@PathVariable("articleId") Long articleId, String message, String conversationId) {
        SseEmitter emitter = new SseEmitter(300000L);
        final Long userId = getSysUser().getUserId();

        try {
            if (userId == null) {
                emitter.send(SseEmitter.event().data("请先登录后再使用此功能").build());
                emitter.complete();
                return emitter;
            }
            
            // 获取文章内容
            StudyArticle article = articleService.selectArticleById(articleId);
            if(article == null) {
                emitter.send(SseEmitter.event().data("文章不存在").build());
                emitter.complete();
                return emitter;
            }

            // 清理文章内容中的HTML标签
            String cleanTitle = cleanHtmlContent(article.getTitle());
            String cleanContent = cleanHtmlContent(article.getContent());

            final String finalConversationId = StringUtils.isEmpty(conversationId) ? 
                UUID.randomUUID().toString() : conversationId;
            
            // 获取或创建会话历史
            List<Message> messages = conversations.computeIfAbsent(
                finalConversationId,
                k -> {
                    List<Message> newMessages = new ArrayList<>();
                    
                    // 创建带有文章上下文的system消息
                    Message systemMessage = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("你是一个专业的学习助手,现在正在帮助用户理解一篇文章。" +
                                "文章内容如下:\n\n" +
                                "标题：" + cleanTitle + "\n\n" + 
                                cleanContent + "\n\n" +
                                "请基于这篇文章的内容回答用户的问题。如果用户的问题与文章无关,也可以回答其他问题。")
                        .build();
                    newMessages.add(systemMessage);
                    
                    // 保存system消息到数据库
                    StudyAiChat systemChat = new StudyAiChat();
                    systemChat.setConversationId(finalConversationId);
                    systemChat.setUserId(userId);
                    systemChat.setRole("system");
                    systemChat.setContent(systemMessage.getContent());
                    systemChat.setModel(Generation.Models.QWEN_PLUS);
                    systemChat.setStatus("0");
                    systemChat.setCreateTime(new Date());
                    studyAiChatService.insertStudyAiChat(systemChat);
                    
                    return newMessages;
                }
            );
            
            // 添加用户消息
            Message userMessage = tongYiMultiRound.createUserMessage(message);
            messages.add(userMessage);
            
            // 保存用户消息
            StudyAiChat userChat = new StudyAiChat();
            userChat.setConversationId(finalConversationId);
            userChat.setUserId(userId);
            userChat.setRole("user");
            userChat.setContent(message);
            userChat.setModel(Generation.Models.QWEN_PLUS);
            userChat.setStatus("0");
            userChat.setCreateTime(new Date());
            studyAiChatService.insertStudyAiChat(userChat);

            // 创建生成参数
            GenerationParam param = tongYiMultiRound.createStreamGenerationParam(messages);
            
            // 异步处理流式响应
            new Thread(() -> {
                try {
                    Semaphore semaphore = new Semaphore(0);
                    StringBuilder fullContent = new StringBuilder();
                    Long cacheId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                    List<String> contentList = new ArrayList<>();
                    
                    tongYiMultiRound.streamCall(param, new ResultCallback<GenerationResult>() {
                        @Override
                        public void onEvent(GenerationResult message) {
                            try {
                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                fullContent.append(content);
                                contentList.add(content);
                                emitter.send(content);
                            } catch (IOException e) {
                                log.error("发送流式消息失败", e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            log.error("流式对话出错", e);
                            semaphore.release();
                        }

                        @Override
                        public void onComplete() {
                            try {
                                cacheData.put(cacheId, contentList);
                                
                                // 保存助手回复
                                StudyAiChat assistantChat = new StudyAiChat();
                                assistantChat.setConversationId(finalConversationId);
                                assistantChat.setUserId(userId);
                                assistantChat.setRole("assistant");
                                assistantChat.setContent(fullContent.toString());
                                assistantChat.setModel(Generation.Models.QWEN_PLUS);
                                assistantChat.setStatus("0");
                                assistantChat.setCreateTime(new Date());
                                studyAiChatService.insertStudyAiChat(assistantChat);
                                
                                // 发送缓存ID事件
                                emitter.send("event: cache-id\ndata: " + cacheId + "\n\n");
                                
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("完成流式对话失败", e);
                            }
                            semaphore.release();
                        }
                    });
                    
                    semaphore.acquire();
                    
                } catch (Exception e) {
                    log.error("处理流式对话失败", e);
                    try {
                        emitter.send(SseEmitter.event().data("处理失败，请稍后重试").build());
                        emitter.complete();
                    } catch (IOException ex) {
                        log.error("发送错误消息失败", ex);
                    }
                }
            }).start();
            
        } catch (Exception e) {
            log.error("创建流式对话失败", e);
            try {
                emitter.send(SseEmitter.event().data("系统错误").build());
                emitter.complete();
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
        }
        
        return emitter;
    }

    @GetMapping("/lesson/chat/{lessonId}")
    public SseEmitter lessonChatStream(@PathVariable("lessonId") Long lessonId, String message, String conversationId) {
        SseEmitter emitter = new SseEmitter(300000L);
        final Long userId = getSysUser().getUserId();

        try {
            if (userId == null) {
                emitter.send(SseEmitter.event().data("请先登录后再使用此功能").build());
                emitter.complete();
                return emitter;
            }
            
            // 获取课程内容
            StudyLesson lesson = lessonService.selectStudyLessonByLessonId(lessonId);
            if(lesson == null) {
                emitter.send(SseEmitter.event().data("课程不存在").build());
                emitter.complete();
                return emitter;
            }

            // 获取字幕文本
            String subtitleText = lesson.getVideoSubtitleText();
            if(StringUtils.isEmpty(subtitleText)) {
                emitter.send(SseEmitter.event().data("该视频暂无字幕文本，无法进行分析").build());
                emitter.complete();
                return emitter;
            }

            final String finalConversationId = StringUtils.isEmpty(conversationId) ? 
                UUID.randomUUID().toString() : conversationId;
            
            // 获取或创建会话历史
            List<Message> messages = conversations.computeIfAbsent(
                finalConversationId,
                k -> {
                    List<Message> newMessages = new ArrayList<>();
                    
                    // 创建带有课程上下文的system消息
                    Message systemMessage = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("你是一个专业的学习助手,现在正在帮助用户理解一个视频课程。" +
                                "课程内容如下:\n\n" +
                                "标题：" + lesson.getTitle() + "\n\n" + 
                                "视频字幕文本：\n" + subtitleText + "\n\n" +
                                "请基于这个课程的内容回答用户的问题。如果用户的问题与课程无关,也可以回答其他问题。")
                        .build();
                    newMessages.add(systemMessage);
                    
                    // 保存system消息到数据库
                    StudyAiChat systemChat = new StudyAiChat();
                    systemChat.setConversationId(finalConversationId);
                    systemChat.setUserId(userId);
                    systemChat.setRole("system");
                    systemChat.setContent(systemMessage.getContent());
                    systemChat.setModel(Generation.Models.QWEN_PLUS);
                    systemChat.setStatus("0");
                    systemChat.setCreateTime(new Date());
                    studyAiChatService.insertStudyAiChat(systemChat);
                    
                    return newMessages;
                }
            );
            
            // 添加用户消息
            Message userMessage = tongYiMultiRound.createUserMessage(message);
            messages.add(userMessage);
            
            // 保存用户消息
            StudyAiChat userChat = new StudyAiChat();
            userChat.setConversationId(finalConversationId);
            userChat.setUserId(userId);
            userChat.setRole("user");
            userChat.setContent(message);
            userChat.setModel(Generation.Models.QWEN_PLUS);
            userChat.setStatus("0");
            userChat.setCreateTime(new Date());
            studyAiChatService.insertStudyAiChat(userChat);

            // 创建生成参数
            GenerationParam param = tongYiMultiRound.createStreamGenerationParam(messages);
            
            // 异步处理流式响应
            new Thread(() -> {
                try {
                    Semaphore semaphore = new Semaphore(0);
                    StringBuilder fullContent = new StringBuilder();
                    Long cacheId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                    List<String> contentList = new ArrayList<>();
                    
                    tongYiMultiRound.streamCall(param, new ResultCallback<GenerationResult>() {
                        @Override
                        public void onEvent(GenerationResult message) {
                            try {
                                String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                fullContent.append(content);
                                contentList.add(content);
                                emitter.send(content);
                            } catch (IOException e) {
                                log.error("发送流式消息失败", e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            log.error("流式对话出错", e);
                            semaphore.release();
                        }

                        @Override
                        public void onComplete() {
                            try {
                                cacheData.put(cacheId, contentList);
                                
                                // 保存助手回复
                                StudyAiChat assistantChat = new StudyAiChat();
                                assistantChat.setConversationId(finalConversationId);
                                assistantChat.setUserId(userId);
                                assistantChat.setRole("assistant");
                                assistantChat.setContent(fullContent.toString());
                                assistantChat.setModel(Generation.Models.QWEN_PLUS);
                                assistantChat.setStatus("0");
                                assistantChat.setCreateTime(new Date());
                                studyAiChatService.insertStudyAiChat(assistantChat);
                                
                                // 发送缓存ID事件
                                emitter.send("event: cache-id\ndata: " + cacheId + "\n\n");
                                
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("完成流式对话失败", e);
                            }
                            semaphore.release();
                        }
                    });
                    
                    semaphore.acquire();
                    
                } catch (Exception e) {
                    log.error("处理流式对话失败", e);
                    try {
                        emitter.send(SseEmitter.event().data("处理失败，请稍后重试").build());
                        emitter.complete();
                    } catch (IOException ex) {
                        log.error("发送错误消息失败", ex);
                    }
                }
            }).start();
            
        } catch (Exception e) {
            log.error("创建流式对话失败", e);
            try {
                emitter.send(SseEmitter.event().data("系统错误").build());
                emitter.complete();
            } catch (IOException ex) {
                log.error("发送错误消息失败", ex);
            }
        }
        
        return emitter;
    }

    private String cleanHtmlContent(String htmlContent) {
        if (StringUtils.isEmpty(htmlContent)) {
            return "";
        }
        
        try {
            // 使用jsoup解析HTML
            Document doc = Jsoup.parse(htmlContent);
            
            // 获取纯文本内容
            String text = doc.text();
            
            // 替换多个空格为单个空格
            text = text.replaceAll("\\s+", " ");
            
            // 处理特殊字符
            text = text.replace("&nbsp;", " ")
                      .replace("\u00A0", " ")  // 处理不间断空格
                      .trim();
            
            return text;
        } catch (Exception e) {
            log.error("清理HTML内容失败", e);
            return htmlContent;
        }
    }

//    // 可选：添加清理超时会话的方法
//    @Scheduled(fixedRate = 3600000) // 每小时执行一次
//    public void cleanupOldConversations() {
//        // 清理3小时前的会话
//        long cutoffTime = System.currentTimeMillis() - 3 * 3600000;
//        conversations.entrySet().removeIf(entry ->
//            entry.getValue().get(entry.getValue().size() - 1).getTimestamp() < cutoffTime);
//    }
}
