package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowFormField;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowFormFieldMapper {

    List<FlowFormField> selectByFormId(@Param("formId") Long formId);

    int insert(FlowFormField field);

    int deleteByFormId(@Param("formId") Long formId);
}