package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import com.jy.study.common.annotation.Excel;
import lombok.Data;

/**
 * 科目对象 study_subject
 */
@Data
public class StudySubject extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 科目ID */
    private Long subjectId;

    /** 科目名称 */
    @Excel(name = "科目名称")
    private String name;

    /** 科目编码 */
    @Excel(name = "科目编码")
    private String code;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
