package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.entity.FlowNode;
import com.tianyu.oasystem.entity.FlowDef;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 流程定义视图（含表单字段与节点） */
@Data
public class FlowDefVO {

    private Long defId;
    private Long formId;
    private String defName;
    private String category;
    private Integer builtIn;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String formName;
    private List<FlowFormField> fields;
    private List<FlowNode> nodes;

    public static FlowDefVO from(FlowDef def, String formName, List<FlowFormField> fields, List<FlowNode> nodes) {
        FlowDefVO vo = new FlowDefVO();
        vo.setDefId(def.getDefId());
        vo.setFormId(def.getFormId());
        vo.setDefName(def.getDefName());
        vo.setCategory(def.getCategory());
        vo.setBuiltIn(def.getBuiltIn());
        vo.setStatus(def.getStatus());
        vo.setCreateTime(def.getCreateTime());
        vo.setFormName(formName);
        vo.setFields(fields);
        vo.setNodes(nodes);
        return vo;
    }
}