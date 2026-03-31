package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class StudyArticle extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long articleId;

    @NotBlank(message = "文章标题不能为空")
    @Size(min = 0, max = 100, message = "文章标题不能超过100个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String status;  

    private Integer sort;

    private String remark;

    @Size(min = 0, max = 50, message = "文章标签长度不能超过50个字符")
    private String tags;
    
    @Size(min = 0, max = 500, message = "文章摘要长度不能超过500个字符")
    private String summary;
    
    private String coverImg;
    
    private Long viewCount;
    
    private Long likeCount;
    
    private Long collectCount;
    
    private String top;

    private String voiceUrl;

    private Long versionId;

    private String versionName;

    private Long subjectId;

    private String subjectName;

    private Long gradeId;

    private String gradeName;

    private Long volumeId;

    private String volumeName;

    private Long cozeId;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("articleId", getArticleId())
                .append("title", getTitle())
                .append("content", getContent())
                .append("status", getStatus())
                .append("sort", getSort())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("tags", getTags())
                .append("summary", getSummary())
                .append("coverImg", getCoverImg())
                .append("viewCount", getViewCount())
                .append("likeCount", getLikeCount())
                .append("collectCount", getCollectCount())
                .append("top", getTop())
                .append("voiceUrl", getVoiceUrl())
                .append("versionId", getVersionId())
                .append("versionName", getVersionName())
                .append("subjectId", getSubjectId())
                .append("subjectName", getSubjectName())
                .append("gradeId", getGradeId())
                .append("gradeName", getGradeName())
                .append("volumeId", getVolumeId())
                .append("volumeName", getVolumeName())
                .append("cozeId", getCozeId())
                .toString();
    }
}
