package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.entity.FlowForm;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 表单定义视图（含字段） */
@Data
public class FlowFormVO {

    private Long formId;
    private String formName;
    private String formKey;
    private String remark;
    private Integer builtIn;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private List<FlowFormField> fields;

    public static FlowFormVO from(FlowForm form, List<FlowFormField> fields) {
        FlowFormVO vo = new FlowFormVO();
        vo.setFormId(form.getFormId());
        vo.setFormName(form.getFormName());
        vo.setFormKey(form.getFormKey());
        vo.setRemark(form.getRemark());
        vo.setBuiltIn(form.getBuiltIn());
        vo.setStatus(form.getStatus());
        vo.setCreateTime(form.getCreateTime());
        vo.setFields(fields);
        return vo;
    }
}