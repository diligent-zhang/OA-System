package com.tianyu.oasystem.entity;

import lombok.Data;

@Data
public class FlowFormField {

    private Long fieldId;
    private Long formId;
    private String fieldKey;
    private String label;
    private String fieldType;
    private String options;
    private Integer required;
    private Integer sortNo;
}