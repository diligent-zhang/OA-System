package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowForm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowFormMapper {

    FlowForm selectById(@Param("formId") Long formId);

    List<FlowForm> selectAll();

    List<FlowForm> selectEnabled();

    int insert(FlowForm form);

    int update(FlowForm form);

    int deleteById(@Param("formId") Long formId);

    int countByDef(@Param("formId") Long formId);
}