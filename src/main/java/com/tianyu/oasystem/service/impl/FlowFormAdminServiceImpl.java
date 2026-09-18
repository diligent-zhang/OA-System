package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.FlowFieldDTO;
import com.tianyu.oasystem.dto.FlowFormDTO;
import com.tianyu.oasystem.entity.FlowForm;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.mapper.FlowFormFieldMapper;
import com.tianyu.oasystem.mapper.FlowFormMapper;
import com.tianyu.oasystem.service.FlowFormAdminService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.FlowFormVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowFormAdminServiceImpl implements FlowFormAdminService {

    private static final Set<String> FIELD_TYPES =
            Set.of("TEXT", "NUMBER", "DATE", "SELECT", "MULTI_SELECT", "ATTACHMENT");

    private final FlowFormMapper formMapper;
    private final FlowFormFieldMapper fieldMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<FlowFormVO> listAll() {
        return formMapper.selectAll().stream()
                .map(f -> FlowFormVO.from(f, fieldMapper.selectByFormId(f.getFormId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlowFormVO> listEnabled() {
        return formMapper.selectEnabled().stream()
                .map(f -> FlowFormVO.from(f, fieldMapper.selectByFormId(f.getFormId())))
                .collect(Collectors.toList());
    }

    @Override
    public FlowFormVO getForm(Long formId) {
        FlowForm form = requireForm(formId);
        return FlowFormVO.from(form, fieldMapper.selectByFormId(formId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createForm(FlowFormDTO dto) {
        FlowForm form = new FlowForm();
        form.setFormName(dto.getFormName());
        form.setFormKey("CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        form.setRemark(dto.getRemark());
        form.setBuiltIn(0);
        form.setStatus(1);
        form.setCreateBy(UserContext.userId());
        formMapper.insert(form);
        saveFields(form.getFormId(), dto.getFields());
        return form.getFormId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateForm(Long formId, FlowFormDTO dto) {
        FlowForm form = requireForm(formId);
        if (form.getBuiltIn() != null && form.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "内置表单不允许修改");
        }
        form.setFormName(dto.getFormName());
        form.setRemark(dto.getRemark());
        formMapper.update(form);
        fieldMapper.deleteByFormId(formId);
        saveFields(formId, dto.getFields());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForm(Long formId) {
        FlowForm form = requireForm(formId);
        if (form.getBuiltIn() != null && form.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "内置表单不允许删除");
        }
        if (formMapper.countByDef(formId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该表单已被流程引用，请先解除引用");
        }
        fieldMapper.deleteByFormId(formId);
        formMapper.deleteById(formId);
    }

    @Override
    public void toggleForm(Long formId) {
        FlowForm form = requireForm(formId);
        formMapper.update(toStatusEntity(form, form.getStatus() == 1 ? 0 : 1));
    }

    private void saveFields(Long formId, List<FlowFieldDTO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "表单至少需要一个字段");
        }
        Set<String> keys = new HashSet<>();
        int sort = 1;
        for (FlowFieldDTO f : fields) {
            if (f.getFieldKey() == null || f.getFieldKey().isBlank()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "存在未命名字段");
            }
            if (!keys.add(f.getFieldKey())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "字段键重复: " + f.getFieldKey());
            }
            if (!FIELD_TYPES.contains(f.getFieldType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "不支持的字段类型: " + f.getFieldType());
            }
            FlowFormField field = new FlowFormField();
            field.setFormId(formId);
            field.setFieldKey(f.getFieldKey());
            field.setLabel(f.getLabel());
            field.setFieldType(f.getFieldType());
            field.setRequired(Boolean.TRUE.equals(f.getRequired()) ? 1 : 0);
            field.setSortNo(sort++);
            try {
                field.setOptions(CollectionUtils.isEmpty(f.getOptions()) ? null
                        : objectMapper.writeValueAsString(f.getOptions()));
            } catch (JsonProcessingException e) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "候选值格式错误");
            }
            fieldMapper.insert(field);
        }
    }

    private FlowForm toStatusEntity(FlowForm form, int status) {
        FlowForm entity = new FlowForm();
        entity.setFormId(form.getFormId());
        entity.setFormName(form.getFormName());
        entity.setRemark(form.getRemark());
        entity.setStatus(status);
        return entity;
    }

    private FlowForm requireForm(Long formId) {
        FlowForm form = formMapper.selectById(formId);
        if (form == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "表单不存在");
        }
        return form;
    }

    /** 获取表单字段（供流程/发起使用），避免每个服务重复依赖 formMapper */
    public List<FlowFormField> loadFields(Long formId) {
        return fieldMapper.selectByFormId(formId);
    }
}