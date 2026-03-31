package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import lombok.Data;

@Data
public class StudyExercise extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long knowledgePointId;

    private Long versionId;

    private String versionName;

    private Long subjectId;

    private String subjectName;

    private Long gradeId;

    private String gradeName;

    private Long volumeId;

    private String volumeName;

    private String type;

    private String content;

    private String options;

    private String answer;

    private String explanation;
}
