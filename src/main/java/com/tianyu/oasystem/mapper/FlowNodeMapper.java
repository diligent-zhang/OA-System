package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowNodeMapper {

    List<FlowNode> selectByDefId(@Param("defId") Long defId);

    int insert(FlowNode node);

    int update(FlowNode node);

    int deleteByDefId(@Param("defId") Long defId);
}