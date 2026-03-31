package com.jy.study.lesson.domain;

import com.jy.study.common.annotation.Excel;
import com.jy.study.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 课程练习对象 study_lesson_exercise
 *
 * @author jily
 * @date 2026-03-26
 */
public class StudyLessonExercise extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 练习 ID */
    private Long exerciseId;

    /** 课程 ID */
    @Excel(name = "课程 ID")
    private Long lessonId;

    /** 课程标题（冗余查询字段） */
    @Excel(name = "所属课程")
    private String lessonTitle;

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

    /** 练习标题 */
    @Excel(name = "练习标题")
    private String title;

    /** 题目类型（1 单选题 2 多选题 3 判断题 4 简答题） */
    @Excel(name = "题目类型", readConverterExp = "1=单选题，2=多选题，3=判断题，4=简答题")
    private String exerciseType;

    /** 选项（JSON 格式） */
    @Excel(name = "选项")
    private String options;

    /** 正确答案 */
    @Excel(name = "正确答案")
    private String answer;

    /** 答案解析 */
    @Excel(name = "答案解析")
    private String analysis;

    /** 分值 */
    @Excel(name = "分值")
    private Integer score;

    /** 难度（1 简单 2 中等 3 困难） */
    @Excel(name = "难度", readConverterExp = "1=简单，2=中等，3=困难")
    private String difficulty;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;

    /** 状态（0 正常 1 停用） */
    @Excel(name = "状态", readConverterExp = "0=正常，1=停用")
    private String status;

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("exerciseId", getExerciseId())
            .append("lessonId", getLessonId())
            .append("lessonTitle", getLessonTitle())
            .append("versionId", getVersionId())
            .append("versionName", getVersionName())
            .append("subjectId", getSubjectId())
            .append("subjectName", getSubjectName())
            .append("gradeId", getGradeId())
            .append("gradeName", getGradeName())
            .append("volumeId", getVolumeId())
            .append("volumeName", getVolumeName())
            .append("title", getTitle())
            .append("exerciseType", getExerciseType())
            .append("options", getOptions())
            .append("answer", getAnswer())
            .append("analysis", getAnalysis())
            .append("score", getScore())
            .append("difficulty", getDifficulty())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
