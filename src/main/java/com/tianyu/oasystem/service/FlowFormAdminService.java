package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.FlowFormDTO;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.vo.FlowFormVO;

import java.util.List;

/** 审批表单定义管理（管理员） */
public interface FlowFormAdminService {

    List<FlowFormVO> listAll();

    List<FlowFormVO> listEnabled();

    FlowFormVO getForm(Long formId);

    Long createForm(FlowFormDTO dto);

    void updateForm(Long formId, FlowFormDTO dto);

    void deleteForm(Long formId);

    void toggleForm(Long formId);

    /** 加载表单字段（供流程/引擎复用） */
    List<FlowFormField> loadFields(Long formId);
}