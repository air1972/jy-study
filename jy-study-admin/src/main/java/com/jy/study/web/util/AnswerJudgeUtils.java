package com.jy.study.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 前台题目判题工具。
 * 统一处理题库题、文章试题、课程习题的答案标准化与比对，避免不同入口规则不一致。
 */
public final class AnswerJudgeUtils {

    private static final Pattern BRACKET_PATTERN = Pattern.compile("[（(【\\[]([^（）()【】\\[\\]]+)[）)】\\]]");
    private static final Pattern CHOICE_LINE_PATTERN = Pattern.compile("^([A-Z])\\s*[\\.．、:：\\)]\\s*(.*)$");
    private static final Pattern CHOICE_TOKEN_PATTERN = Pattern.compile("(^|[^A-Z])([A-Z])([^A-Z]|$)");
    private static final List<String> DEFAULT_CHOICE_KEYS = Arrays.asList("A", "B", "C", "D", "E", "F");

    private AnswerJudgeUtils() {
    }

    public static boolean judgeStudyExerciseAnswer(String type, String expected, String actual, String optionsRaw) {
        if ("1".equals(type)) {
            return judgeChoiceAnswer(expected, actual, optionsRaw, false);
        }
        if ("2".equals(type)) {
            return judgeFillBlankAnswer(expected, actual);
        }
        if ("3".equals(type)) {
            return judgeTrueFalseAnswer(expected, actual);
        }
        return judgeSubjectiveAnswer(expected, actual);
    }

    public static boolean judgeLessonExerciseAnswer(String type, String expected, String actual, String optionsRaw) {
        if ("1".equals(type)) {
            return judgeChoiceAnswer(expected, actual, optionsRaw, false);
        }
        if ("2".equals(type)) {
            return judgeChoiceAnswer(expected, actual, optionsRaw, true);
        }
        if ("3".equals(type)) {
            return judgeTrueFalseAnswer(expected, actual);
        }
        return judgeSubjectiveAnswer(expected, actual);
    }

    private static boolean judgeChoiceAnswer(String expected, String actual, String optionsRaw, boolean multipleChoice) {
        Map<String, String> options = parseOptions(optionsRaw);
        Set<String> expectedTokens = resolveChoiceTokens(expected, options);
        Set<String> actualTokens = resolveChoiceTokens(actual, options);
        if (!expectedTokens.isEmpty() && !actualTokens.isEmpty()) {
            if (!multipleChoice && actualTokens.size() != 1) {
                return false;
            }
            return expectedTokens.equals(actualTokens);
        }
        for (String variant : collectTextVariants(expected, true)) {
            if (matchesExactOrNear(variant, actual, 0.96D, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean judgeTrueFalseAnswer(String expected, String actual) {
        String normalizedExpected = normalizeTrueFalse(expected);
        String normalizedActual = normalizeTrueFalse(actual);
        return !normalizedExpected.isEmpty() && normalizedExpected.equals(normalizedActual);
    }

    private static boolean judgeFillBlankAnswer(String expected, String actual) {
        for (String variant : collectTextVariants(expected, true)) {
            if (matchesExactOrNear(variant, actual, 0.92D, false)) {
                return true;
            }
        }
        return false;
    }

    private static boolean judgeSubjectiveAnswer(String expected, String actual) {
        for (String variant : collectTextVariants(expected, true)) {
            if (matchesExactOrNear(variant, actual, 0.90D, true)) {
                return true;
            }
            if (containsAllKeywords(variant, actual)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> parseOptions(String optionsRaw) {
        if (isBlank(optionsRaw)) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        try {
            Object parsed = JSON.parse(optionsRaw);
            if (parsed instanceof JSONObject) {
                JSONObject object = (JSONObject) parsed;
                for (Map.Entry<String, Object> entry : object.entrySet()) {
                    String key = safeTrim(entry.getKey()).toUpperCase(Locale.ROOT);
                    String value = entry.getValue() == null ? "" : safeTrim(String.valueOf(entry.getValue()));
                    if (!key.isEmpty() && !value.isEmpty()) {
                        result.put(key, value);
                    }
                }
            } else if (parsed instanceof JSONArray) {
                JSONArray array = (JSONArray) parsed;
                for (int i = 0; i < array.size(); i++) {
                    String key = String.valueOf((char) ('A' + i));
                    String value = safeTrim(array.getString(i));
                    if (!value.isEmpty()) {
                        result.put(key, value);
                    }
                }
            }
        } catch (Exception ignored) {
            // 降级走文本解析
        }
        if (!result.isEmpty()) {
            return result;
        }

        String[] lines = stripAnswerDecorations(optionsRaw).split("\\r?\\n");
        for (String line : lines) {
            String candidate = safeTrim(line).toUpperCase(Locale.ROOT);
            Matcher matcher = CHOICE_LINE_PATTERN.matcher(candidate);
            if (matcher.find()) {
                result.put(matcher.group(1), safeTrim(line.substring(1).replaceFirst("^[\\.．、:：\\)]\\s*", "")));
            }
        }
        return result;
    }

    private static Set<String> resolveChoiceTokens(String raw, Map<String, String> options) {
        if (isBlank(raw)) {
            return Collections.emptySet();
        }
        LinkedHashSet<String> tokens = new LinkedHashSet<String>();
        List<String> validKeys = new ArrayList<String>(options.isEmpty() ? DEFAULT_CHOICE_KEYS : options.keySet());
        String cleaned = toHalfWidth(stripAnswerDecorations(raw)).toUpperCase(Locale.ROOT);
        String collapsed = cleaned
                .replaceAll("(正确答案|参考答案|答案是|答案|选项|选|项|OPTION|ANSWER)", "")
                .replaceAll("[\\s,，、;；/|]+", "");
        String allowedKeys = buildAllowedKeys(validKeys);
        if (!collapsed.isEmpty() && !allowedKeys.isEmpty() && collapsed.matches("[" + allowedKeys + "]+")) {
            for (int i = 0; i < collapsed.length(); i++) {
                String key = String.valueOf(collapsed.charAt(i));
                if (validKeys.contains(key)) {
                    tokens.add(key);
                }
            }
        }
        if (!tokens.isEmpty()) {
            return tokens;
        }

        Matcher matcher = CHOICE_TOKEN_PATTERN.matcher(cleaned);
        while (matcher.find()) {
            String key = matcher.group(2);
            if (validKeys.contains(key)) {
                tokens.add(key);
            }
        }
        if (!tokens.isEmpty()) {
            return tokens;
        }

        if (options.isEmpty()) {
            return tokens;
        }
        String normalizedRaw = normalizeComparableText(raw);
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String normalizedOption = normalizeComparableText(entry.getValue());
            if (!normalizedRaw.isEmpty() && normalizedRaw.equals(normalizedOption)) {
                tokens.add(entry.getKey());
                return tokens;
            }
        }

        String[] fragments = stripAnswerDecorations(raw).split("[,，、;；/|\\r\\n]+");
        for (String fragment : fragments) {
            String normalizedFragment = normalizeComparableText(fragment);
            if (normalizedFragment.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String> entry : options.entrySet()) {
                String normalizedOption = normalizeComparableText(entry.getValue());
                if (normalizedFragment.equals(normalizedOption)) {
                    tokens.add(entry.getKey());
                }
            }
        }
        return tokens;
    }

    private static String buildAllowedKeys(List<String> keys) {
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            if (key != null && key.length() == 1 && Character.isLetter(key.charAt(0))) {
                builder.append(Character.toUpperCase(key.charAt(0)));
            }
        }
        return builder.toString();
    }

    private static List<String> collectTextVariants(String raw, boolean allowAlternativeSplit) {
        LinkedHashSet<String> variants = new LinkedHashSet<String>();
        String base = stripAnswerDecorations(raw);
        collectSingleVariant(variants, base);
        if (allowAlternativeSplit) {
            for (String part : splitAlternativeAnswers(base)) {
                collectSingleVariant(variants, part);
            }
        }
        return new ArrayList<String>(variants);
    }

    private static void collectSingleVariant(Set<String> variants, String candidate) {
        String text = safeTrim(candidate);
        if (text.isEmpty()) {
            return;
        }
        variants.add(text);

        Matcher matcher = BRACKET_PATTERN.matcher(text);
        while (matcher.find()) {
            String inside = safeTrim(matcher.group(1));
            if (!inside.isEmpty()) {
                variants.add(inside);
            }
        }
        String outside = safeTrim(text.replaceAll("[（(【\\[][^（）()【】\\[\\]]+[）)】\\]]", " "));
        if (!outside.isEmpty()) {
            variants.add(outside);
        }
    }

    private static List<String> splitAlternativeAnswers(String text) {
        String base = safeTrim(text);
        if (base.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = base.split("\\s*(?:\\||｜|/|／|或者|或)\\s*");
        if (parts.length <= 1) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String part : parts) {
            if (!isBlank(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private static boolean matchesExactOrNear(String expected, String actual, double threshold, boolean allowContainMatch) {
        String normalizedExpected = normalizeComparableText(expected);
        String normalizedActual = normalizeComparableText(actual);
        if (normalizedExpected.isEmpty() || normalizedActual.isEmpty()) {
            return false;
        }
        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }
        if (allowContainMatch && normalizedExpected.length() >= 2 && normalizedActual.contains(normalizedExpected)) {
            return true;
        }
        if (normalizedExpected.length() >= 4 && normalizedActual.length() >= 4) {
            return similarity(normalizedExpected, normalizedActual) >= threshold;
        }
        return false;
    }

    private static boolean containsAllKeywords(String expected, String actual) {
        String normalizedActual = normalizeComparableText(actual);
        if (normalizedActual.isEmpty()) {
            return false;
        }
        List<String> keywords = extractKeywords(expected);
        if (keywords.size() < 2) {
            return false;
        }
        for (String keyword : keywords) {
            if (!normalizedActual.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> extractKeywords(String text) {
        String base = toHalfWidth(stripAnswerDecorations(text)).toLowerCase(Locale.ROOT);
        String[] parts = base.split("(?:[，,；;。.!！？、\\r\\n]|以及|并且|而且|和|及|与)+");
        LinkedHashSet<String> keywords = new LinkedHashSet<String>();
        for (String part : parts) {
            String normalized = normalizeComparableText(part);
            if (normalized.length() >= 2) {
                keywords.add(normalized);
            }
        }
        return new ArrayList<String>(keywords);
    }

    private static String normalizeTrueFalse(String text) {
        String normalized = normalizeComparableText(text);
        if (normalized.isEmpty()) {
            return "";
        }
        if ("true".equals(normalized) || "t".equals(normalized) || "1".equals(normalized)
                || "yes".equals(normalized) || "y".equals(normalized)
                || "对".equals(normalized) || "正确".equals(normalized) || "是".equals(normalized)
                || "√".equals(normalized) || "v".equals(normalized)) {
            return "T";
        }
        if ("false".equals(normalized) || "f".equals(normalized) || "0".equals(normalized)
                || "no".equals(normalized) || "n".equals(normalized)
                || "错".equals(normalized) || "错误".equals(normalized) || "否".equals(normalized)
                || "×".equals(normalized) || "x".equals(normalized)) {
            return "F";
        }
        return normalized;
    }

    private static String normalizeComparableText(String text) {
        String normalized = toHalfWidth(stripAnswerDecorations(text)).toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("<[^>]+>", "");
        normalized = normalized.replaceAll("[\\s]+", "");
        normalized = normalized.replaceAll("[\\p{Punct}，。！？；：、（）()【】\\[\\]《》“”‘’\"'`…—·]+", "");
        return normalized;
    }

    private static String stripAnswerDecorations(String text) {
        String value = safeTrim(toHalfWidth(text));
        if (value.isEmpty()) {
            return "";
        }
        value = value.replaceFirst("(?i)^\\s*(?:\\[)?(?:正确答案|参考答案|答案|答|answer)(?:\\])?\\s*[:：]?\\s*", "");
        value = value.replaceFirst("^\\s*[（(【\\[]?(?:正确答案|参考答案|答案|答)[）)】\\]]?\\s*[:：]?\\s*", "");
        return safeTrim(value);
    }

    private static double similarity(String a, String b) {
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) {
            return 1D;
        }
        int distance = levenshteinDistance(a, b);
        return 1D - (double) distance / (double) maxLength;
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    private static String toHalfWidth(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 12288) {
                chars[i] = ' ';
            } else if (chars[i] >= 65281 && chars[i] <= 65374) {
                chars[i] = (char) (chars[i] - 65248);
            }
        }
        return new String(chars);
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static String safeTrim(String text) {
        return text == null ? "" : text.trim();
    }
}
