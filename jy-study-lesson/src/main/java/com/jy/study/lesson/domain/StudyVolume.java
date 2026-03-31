package com.jy.study.lesson.domain;

import com.jy.study.common.core.domain.BaseEntity;
import com.jy.study.common.annotation.Excel;
import lombok.Data;

/**
 * 册子对象 study_volume
 */
@Data
public class StudyVolume extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 册子ID */
    private Long volumeId;

    /** 册子名称 */
    @Excel(name = "册子名称")
    private String name;

    /** 册子编码 */
    @Excel(name = "册子编码")
    private String code;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
