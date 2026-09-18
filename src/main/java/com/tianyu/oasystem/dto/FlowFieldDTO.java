package com.tianyu.oasystem.dto;

import lombok.Data;

import java.util.List;

/** 表单字段定义（表单设计用） */
@Data
public class FlowFieldDTO {

    private String fieldKey;
    private String label;
    /** TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/ATTACHMENT */
    private String fieldType;
    private List<String> options;
    private Boolean required;
}