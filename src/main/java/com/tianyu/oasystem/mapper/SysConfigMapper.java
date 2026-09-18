package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysConfigMapper {

    SysConfig selectByKey(@Param("configKey") String configKey);

    List<SysConfig> selectAll();

    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);
}