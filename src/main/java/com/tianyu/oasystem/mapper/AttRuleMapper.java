package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.AttRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttRuleMapper {

    AttRule selectById(@Param("ruleId") Long ruleId);

    List<AttRule> selectAll();

    /** 当前生效规则（单条） */
    AttRule selectActive();

    int insert(AttRule rule);

    int update(AttRule rule);

    int updateEnabled(@Param("ruleId") Long ruleId, @Param("enabled") Integer enabled);

    int deleteById(@Param("ruleId") Long ruleId);
}