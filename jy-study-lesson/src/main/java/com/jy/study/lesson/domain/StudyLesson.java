package com.jy.study.lesson.domain;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.jy.study.common.annotation.Excel;
import com.jy.study.common.core.domain.BaseEntity;

/**
 * 课程对象 study_lesson
 *
 * @author jily
 * @date 2025-01-14
 */
public class StudyLesson extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 课程 ID */
    private Long lessonId;

    /** 课程标题 */
    @Excel(name = "课程标题")
    private String title;

    /** 课程封面图片 */
    @Excel(name = "课程封面图片")
    private String coverImg;

    /** 课程描述 */
    @Excel(name = "课程描述")
    private String description;

    /** 版本ID */
    @Excel(name = "版本ID")
    private Long versionId;

    /** 版本名称 */
    @Excel(name = "版本名称")
    private String versionName;

    /** 科目ID */
    @Excel(name = "科目ID")
    private Long subjectId;

    /** 科目名称 */
    @Excel(name = "科目名称")
    private String subjectName;

    /** 年级ID */
    @Excel(name = "年级ID")
    private Long gradeId;

    /** 年级名称 */
    @Excel(name = "年级名称")
    private String gradeName;

    /** 册子ID */
    @Excel(name = "册子ID")
    private Long volumeId;

    /** 册子名称 */
    @Excel(name = "册子名称")
    private String volumeName;

    /** 课程标签 (逗号分隔) */
    @Excel(name = "课程标签 (逗号分隔)")
    private String tags;

    /** 状态（0 正常 1 停用） */
    @Excel(name = "状态", readConverterExp = "0=正常，1=停用")
    private String status;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;

    /** 浏览量 */
    @Excel(name = "浏览量")
    private Long viewCount;


    /** 点赞数 */
    @Excel(name = "点赞数")
    private Long likeCount;

    /** 收藏数 */
    @Excel(name = "收藏数")
    private Long collectCount;

    /** 课程视频 URL */
    @Excel(name = "课程视频 URL")
    private String videoUrl;

    /** 视频字幕识别文本 */
    @Excel(name = "视频字幕识别文本")
    private String videoSubtitleText;

    /** 练习列表（非数据库字段） */
    private List<StudyLessonExercise> exercises;

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverImg() {
        return coverImg;
    }

    public void setCoverImg(String coverImg) {
        this.coverImg = coverImg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Long volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeName() {
        return volumeName;
    }

    public void setVolumeName(String volumeName) {
        this.volumeName = volumeName;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }


    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }

    public Long getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Long collectCount) {
        this.collectCount = collectCount;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoSubtitleText() {
        return videoSubtitleText;
    }

    public void setVideoSubtitleText(String videoSubtitleText) {
        this.videoSubtitleText = videoSubtitleText;
    }

    public List<StudyLessonExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<StudyLessonExercise> exercises) {
        this.exercises = exercises;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("lessonId", getLessonId())
                .append("title", getTitle())
                .append("coverImg", getCoverImg())
                .append("description", getDescription())
                .append("versionId", getVersionId())
                .append("versionName", getVersionName())
                .append("subjectId", getSubjectId())
                .append("subjectName", getSubjectName())
                .append("gradeId", getGradeId())
                .append("gradeName", getGradeName())
                .append("volumeId", getVolumeId())
                .append("volumeName", getVolumeName())
                .append("tags", getTags())
                .append("status", getStatus())
                .append("sort", getSort())
                .append("viewCount", getViewCount())
                .append("likeCount", getLikeCount())
                .append("collectCount", getCollectCount())
                .append("videoUrl", getVideoUrl())
                .append("videoSubtitleText", getVideoSubtitleText())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
