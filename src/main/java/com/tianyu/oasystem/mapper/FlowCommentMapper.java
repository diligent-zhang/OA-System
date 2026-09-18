package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowCommentMapper {

    int insert(FlowComment comment);

    List<FlowComment> selectByInstance(@Param("instanceId") Long instanceId);
}