package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 在线聊天室敏感词对象 study_online_chat_sensitive_word
 */
public class StudyOnlineChatSensitiveWord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 敏感词ID */
    private Long wordId;

    /** 敏感词 */
    private String word;

    /** 替换文本 */
    private String replaceText;

    /** 状态（0生效 1失效） */
    private String status;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getReplaceText() {
        return replaceText;
    }

    public void setReplaceText(String replaceText) {
        this.replaceText = replaceText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("wordId", getWordId())
                .append("word", getWord())
                .append("replaceText", getReplaceText())
                .append("status", getStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
