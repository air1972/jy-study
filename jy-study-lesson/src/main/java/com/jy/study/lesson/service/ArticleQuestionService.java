package com.jy.study.lesson.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jy.study.lesson.domain.StudyAiCoze;
import com.jy.study.lesson.domain.StudyArticle;
import com.jy.study.lesson.domain.StudyExercise;
import com.jy.study.lesson.domain.StudyKnowledgePoint;
import com.jy.study.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jy.study.common.ai.TongYiAI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ArticleQuestionService {

    @Autowired
    private TongYiAI tongYiAI;

    @Autowired
    private IStudyAiCozeService aiCozeService;

    @Autowired
    private IStudyArticleService articleService;

    @Autowired
    private IStudyKnowledgePointService knowledgePointService;

    @Autowired
    private IStudyExerciseService exerciseService;

    /**
     * 处理 AI 生成的试题内容，去除转义字符
     */
    public String cleanQuestionContent(String content) {
        if (content == null) {
            return null;
        }
        // 去除 JSON 字符串外面的 markdown 代码块标记
        content = content.replace("```json", "").replace("```", "").trim();
        return content;
    }

    /**
     * 格式化试题内容，添加HTML样式
     */
    public String formatQuestionHtml(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder html = new StringBuilder();
        // 匹配中文数字加顿号作为章节开始，如：一、 二、 ...
        String[] sections = content.split("(?=[一二三四五六七八九十]+、)");
        
        for (String section : sections) {
            if (section.trim().isEmpty()) {
                continue;
            }
            
            String[] parts = section.split("\n", 2);
            String title = parts[0].trim();
            String questions = parts.length > 1 ? parts[1] : "";
            
            // 创建题型区块
            html.append("<div class=\"question-section\">");
            html.append("<h3 class=\"section-title\">").append(title).append("</h3>");
            
            // 处理每道题目，匹配数字加顿号作为题目开始，如：1、 2、 ...
            String[] questionItems = questions.split("(?=\\d+、)");
            for (String item : questionItems) {
                if (item.trim().isEmpty()) {
                    continue;
                }
                
                html.append("<div class=\"question-item\">");
                
                // 分离题目内容、答案和解析
                String questionText = "";
                String answer = "";
                String explanation = "";
                
                // 处理题目内容
                int answerIndex = item.indexOf("[答案]");
                int correctAnswerIndex = item.indexOf("[正确答案]");
                int explanationIndex = item.indexOf("[解析]");
                
                int firstAnswerIndex = -1;
                int answerLabelLength = 0;
                
                if (answerIndex != -1 && (correctAnswerIndex == -1 || answerIndex < correctAnswerIndex)) {
                    firstAnswerIndex = answerIndex;
                    answerLabelLength = 4;
                } else if (correctAnswerIndex != -1) {
                    firstAnswerIndex = correctAnswerIndex;
                    answerLabelLength = 6;
                }

                if (firstAnswerIndex != -1) {
                    questionText = item.substring(0, firstAnswerIndex).trim();
                    if (explanationIndex != -1 && explanationIndex > firstAnswerIndex) {
                        answer = item.substring(firstAnswerIndex + answerLabelLength, explanationIndex).trim();
                        explanation = item.substring(explanationIndex + 4).trim();
                    } else {
                        answer = item.substring(firstAnswerIndex + answerLabelLength).trim();
                    }
                } else {
                    questionText = item;
                }
                
                // 添加题目内容（保持换行）
                html.append("<div class=\"question-content\">");
                String[] lines = questionText.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        html.append(line.trim()).append("<br>");
                    }
                }
                html.append("</div>");
                
                // 添加答案（如果存在）
                if (!answer.isEmpty()) {
                    html.append("<div class=\"question-answer\">");
                    html.append("<strong>答案：</strong>");
                    String[] answerLines = answer.split("\n");
                    for (String line : answerLines) {
                        if (!line.trim().isEmpty()) {
                            html.append(line.trim()).append("<br>");
                        }
                    }
                    html.append("</div>");
                }
                
                // 添加解析（如果存在）
                if (!explanation.isEmpty()) {
                    html.append("<div class=\"question-explanation\">");
                    html.append("<strong>解析：</strong>");
                    String[] explanationLines = explanation.split("\n");
                    for (String line : explanationLines) {
                        if (!line.trim().isEmpty()) {
                            html.append(line.trim()).append("<br>");
                        }
                    }
                    html.append("</div>");
                }
                
                html.append("</div>");
            }
            
            html.append("</div>");
        }
        
        return html.toString();
    }

    public String formatQuestionHtmlForSubmit(Long articleId, String articleTitle, Long cozeId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        Long kpId = ensureArticleKnowledgePoint(articleId, articleTitle);
        List<StudyExercise> exercises = ensureExercisesForCoze(kpId, cozeId, content);
        Map<Integer, StudyExercise> seqMap = buildSeqMap(exercises);

        StringBuilder html = new StringBuilder();
        String[] sections = content.split("(?=[一二三四五六七八九十]+、)");

        int seq = 0;
        for (String section : sections) {
            if (section.trim().isEmpty()) {
                continue;
            }
            String[] parts = section.split("\n", 2);
            String title = parts[0].trim();
            String questions = parts.length > 1 ? parts[1] : "";
            String type = resolveType(title);

            html.append("<div class=\"question-section\">");
            html.append("<h3 class=\"section-title\">").append(title).append("</h3>");

            String[] questionItems = questions.split("(?=\\d+、)");
            for (String item : questionItems) {
                if (item.trim().isEmpty()) {
                    continue;
                }
                seq++;
                StudyExercise ex = seqMap.get(seq);
                Long exId = ex != null ? ex.getId() : null;

                ParsedQuestion pq = parseQuestionItem(item, type);

                html.append("<div class=\"question-item\" data-exercise-id=\"").append(exId == null ? "" : exId)
                        .append("\" data-type=\"").append(type).append("\">");

                html.append("<div class=\"question-content\">");
                for (String line : pq.displayLines) {
                    if (!line.trim().isEmpty()) {
                        html.append(line.trim()).append("<br>");
                    }
                }
                html.append("</div>");

                html.append("<div class=\"question-user-answer\">");
                if ("1".equals(type)) {
                    for (Map.Entry<String, String> opt : pq.options.entrySet()) {
                        html.append("<div class=\"radio\"><label><input type=\"radio\" name=\"ex_")
                                .append(exId == null ? seq : exId)
                                .append("\" value=\"").append(opt.getKey()).append("\"> ")
                                .append(opt.getKey()).append(". ").append(opt.getValue()).append("</label></div>");
                    }
                } else if ("3".equals(type)) {
                    html.append("<div class=\"radio\" style=\"display:inline-block; margin-right:20px;\"><label><input type=\"radio\" name=\"ex_")
                            .append(exId == null ? seq : exId)
                            .append("\" value=\"正确\"> 正确</label></div>");
                    html.append("<div class=\"radio\" style=\"display:inline-block;\"><label><input type=\"radio\" name=\"ex_")
                            .append(exId == null ? seq : exId)
                            .append("\" value=\"错误\"> 错误</label></div>");
                } else if ("2".equals(type)) {
                    html.append("<input type=\"text\" class=\"form-control\" name=\"ex_")
                            .append(exId == null ? seq : exId)
                            .append("\" placeholder=\"请输入您的答案\">");
                } else {
                    html.append("<textarea class=\"form-control\" name=\"ex_")
                            .append(exId == null ? seq : exId)
                            .append("\" rows=\"2\" placeholder=\"请输入您的回答\"></textarea>");
                }
                html.append("</div>");

                html.append("<div class=\"question-answer\" style=\"display:none\"></div>");
                html.append("<div class=\"question-explanation\" style=\"display:none\"></div>");
                html.append("</div>");
            }
            html.append("</div>");
        }
        return html.toString();
    }

    public StudyAiCoze generateQuestions(Long articleId, Integer xuanze, Integer tiankong,
                                         Integer panduan, Integer jianda, String content) {
        int xuanzeNum = safeCount(xuanze, 5);
        int tiankongNum = safeCount(tiankong, 3);
        int panduanNum = safeCount(panduan, 5);
        int jiandaNum = safeCount(jianda, 2);
        int totalNum = xuanzeNum + tiankongNum + panduanNum + jiandaNum;
        if (totalNum <= 0) {
            throw new IllegalArgumentException("题目数量不能全为 0");
        }
        if (StringUtils.isBlank(content)) {
            throw new IllegalArgumentException("文章内容为空，无法生成试题");
        }

        // 1. 创建 AI 试题记录
        StudyAiCoze aiCoze = new StudyAiCoze();
        aiCoze.setArticleId(articleId);
        aiCoze.setQuestionNum(totalNum);
        aiCoze.setStatus("0"); // 0-生成中

        // 保存初始记录
        aiCozeService.insertAiCoze(aiCoze);

        try {
            // 2. 调用通义 AI 生成题目
            String response = tongYiAI.generateQuestions(xuanzeNum, tiankongNum, panduanNum, jiandaNum, content);
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("AI 返回内容为空");
            }

            // 3. 更新 AI 试题记录
            // 处理 content 中的转义字符
            String formattedContent = cleanQuestionContent(response);
            if (StringUtils.isBlank(formattedContent)) {
                throw new RuntimeException("题目内容解析后为空");
            }
            aiCoze.setContent(formattedContent);
            aiCoze.setStatus("1"); // 1-已完成
            aiCozeService.updateAiCoze(aiCoze);

            // 4. 更新文章的 cozeId
            articleService.updateArticleCozeId(articleId, aiCoze.getId());

            StudyArticle article = articleService.selectArticleById(articleId);
            String articleTitle = article != null ? article.getTitle() : "";
            Long kpId = ensureArticleKnowledgePoint(articleId, articleTitle);
            ensureExercisesForCoze(kpId, aiCoze.getId(), formattedContent);

            return aiCoze;
        } catch (Exception e) {
            // 5. 如果生成失败，更新状态
            aiCoze.setStatus("2"); // 2-失败
            String errMsg = StringUtils.isBlank(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
            aiCoze.setContent("生成失败：" + errMsg);
            aiCozeService.updateAiCoze(aiCoze);
            throw new RuntimeException("生成失败：" + errMsg, e);
        }
    }

    private int safeCount(Integer count, int defaultValue) {
        if (count == null) {
            return defaultValue;
        }
        return Math.max(0, count);
    }

    private Long ensureArticleKnowledgePoint(Long articleId, String articleTitle) {
        StudyKnowledgePoint query = new StudyKnowledgePoint();
        query.setContentType("article");
        query.setContentId(articleId);
        List<StudyKnowledgePoint> list = knowledgePointService.selectStudyKnowledgePointList(query);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getId();
        }
        StudyKnowledgePoint kp = new StudyKnowledgePoint();
        kp.setName(StringUtils.isBlank(articleTitle) ? ("文章试题-" + articleId) : ("文章试题：" + articleTitle));
        kp.setDescription("文章试题关联知识点");
        kp.setContentType("article");
        kp.setContentId(articleId);
        kp.setSourceTitle(articleTitle);
        knowledgePointService.insertStudyKnowledgePoint(kp);
        return kp.getId();
    }

    private List<StudyExercise> ensureExercisesForCoze(Long knowledgePointId, Long cozeId, String content) {
        StudyExercise q = new StudyExercise();
        q.setKnowledgePointId(knowledgePointId);
        q.setRemark("aiCozeId=" + cozeId);
        List<StudyExercise> existing = exerciseService.selectStudyExerciseList(q);
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }

        List<ParsedQuestion> parsed = parseAllQuestions(content);
        List<StudyExercise> created = new ArrayList<>();
        int seq = 0;
        for (ParsedQuestion pq : parsed) {
            seq++;
            StudyExercise ex = new StudyExercise();
            ex.setKnowledgePointId(knowledgePointId);
            ex.setType(pq.type);
            ex.setContent(pq.stem);
            ex.setOptions(pq.options.isEmpty() ? null : JSON.toJSONString(pq.options));
            ex.setAnswer(pq.answer);
            ex.setExplanation(pq.explanation);
            ex.setRemark("aiCozeId=" + cozeId + ";q=" + seq);
            exerciseService.insertStudyExercise(ex);
            created.add(ex);
        }
        return created;
    }

    private Map<Integer, StudyExercise> buildSeqMap(List<StudyExercise> list) {
        Map<Integer, StudyExercise> map = new HashMap<>();
        if (list == null) {
            return map;
        }
        Pattern p = Pattern.compile("q=(\\d+)");
        for (StudyExercise ex : list) {
            if (ex == null || ex.getRemark() == null) continue;
            Matcher m = p.matcher(ex.getRemark());
            if (m.find()) {
                map.put(Integer.parseInt(m.group(1)), ex);
            }
        }
        return map;
    }

    private String resolveType(String sectionBody) {
        if (sectionBody.contains("选择")) return "1";
        if (sectionBody.contains("填空")) return "2";
        if (sectionBody.contains("判断")) return "3";
        if (sectionBody.contains("简答")) return "4";
        return "4";
    }

    private static class ParsedQuestion {
        String type;
        String stem;
        LinkedHashMap<String, String> options = new LinkedHashMap<>();
        String answer = "";
        String explanation = "";
        List<String> displayLines = new ArrayList<>();
    }

    private List<ParsedQuestion> parseAllQuestions(String content) {
        List<ParsedQuestion> list = new ArrayList<>();
        String[] sections = content.split("(?=[一二三四五六七八九十]+、)");
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;
            int titleEndIndex = section.indexOf("、");
            if (titleEndIndex == -1) continue;
            String questions = section.substring(titleEndIndex + 1);
            String type = resolveType(questions);
            String[] questionItems = questions.split("(?=\\d+、)");
            for (String item : questionItems) {
                if (item.trim().isEmpty()) continue;
                list.add(parseQuestionItem(item, type));
            }
        }
        return list;
    }

    private ParsedQuestion parseQuestionItem(String item, String type) {
        ParsedQuestion pq = new ParsedQuestion();
        pq.type = type;

        int answerIndex = item.indexOf("[答案]");
        int correctAnswerIndex = item.indexOf("[正确答案]");
        int explanationIndex = item.indexOf("[解析]");

        int firstAnswerIndex = -1;
        int answerLabelLength = 0;
        if (answerIndex != -1 && (correctAnswerIndex == -1 || answerIndex < correctAnswerIndex)) {
            firstAnswerIndex = answerIndex;
            answerLabelLength = 4;
        } else if (correctAnswerIndex != -1) {
            firstAnswerIndex = correctAnswerIndex;
            answerLabelLength = 6;
        }

        String questionText;
        if (firstAnswerIndex != -1) {
            questionText = item.substring(0, firstAnswerIndex).trim();
            if (explanationIndex != -1 && explanationIndex > firstAnswerIndex) {
                pq.answer = item.substring(firstAnswerIndex + answerLabelLength, explanationIndex).trim();
                pq.explanation = item.substring(explanationIndex + 4).trim();
            } else {
                pq.answer = item.substring(firstAnswerIndex + answerLabelLength).trim();
            }
        } else {
            questionText = item.trim();
        }

        String[] lines = questionText.split("\n");
        Pattern optPattern = Pattern.compile("^([A-D])\\s*[\\.．、]\\s*(.*)$");
        StringBuilder stem = new StringBuilder();
        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            Matcher m = optPattern.matcher(t);
            if ("1".equals(type) && m.find()) {
                pq.options.put(m.group(1), m.group(2).trim());
            } else {
                stem.append(t).append("\n");
            }
            pq.displayLines.add(t);
        }
        String s = stem.toString().trim();
        pq.stem = s.replaceFirst("^\\d+、\\s*", "");
        return pq;
    }

    /**
     * 删除试题
     */
    public void deleteQuestions(Long articleId) {
        // 1. 查找与文章相关的 StudyAiCoze 记录
        StudyAiCoze query = new StudyAiCoze();
        query.setArticleId(articleId);
        List<StudyAiCoze> aiCozeList = aiCozeService.selectAiCozeList(query);
        
        if (aiCozeList != null && !aiCozeList.isEmpty()) {
            for (StudyAiCoze aiCoze : aiCozeList) {
                Long cozeId = aiCoze.getId();
                
                // 2. 查找与该 StudyAiCoze 记录相关的 StudyExercise 记录
                StudyExercise exerciseQuery = new StudyExercise();
                exerciseQuery.setRemark("aiCozeId=" + cozeId);
                List<StudyExercise> exerciseList = exerciseService.selectStudyExerciseList(exerciseQuery);
                
                // 3. 删除这些 StudyExercise 记录
                if (exerciseList != null && !exerciseList.isEmpty()) {
                    for (StudyExercise exercise : exerciseList) {
                        exerciseService.deleteStudyExerciseById(exercise.getId());
                    }
                }
                
                // 4. 删除 StudyAiCoze 记录
                aiCozeService.deleteAiCozeById(cozeId);
            }
        }
    }
}
