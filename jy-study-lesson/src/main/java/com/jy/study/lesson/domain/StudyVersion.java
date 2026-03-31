package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import com.jy.study.common.annotation.Excel;
import lombok.Data;

/**
 * 版本对象 study_version
 */
@Data
public class StudyVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 版本名称 */
    @Excel(name = "版本名称")
    private String name;

    /** 版本编码 */
    @Excel(name = "版本编码")
    private String code;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
