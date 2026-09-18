package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowDef;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowDefMapper {

    FlowDef selectById(@Param("defId") Long defId);

    List<FlowDef> selectAll();

    /** 启用中的流程（员工发起用） */
    List<FlowDef> selectEnabled();

    int insert(FlowDef def);

    int update(FlowDef def);

    int updateStatus(@Param("defId") Long defId, @Param("status") Integer status);

    int deleteById(@Param("defId") Long defId);
}