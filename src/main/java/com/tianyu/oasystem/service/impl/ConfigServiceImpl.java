package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.entity.SysConfig;
import com.tianyu.oasystem.mapper.SysConfigMapper;
import com.tianyu.oasystem.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigMapper configMapper;

    @Override
    public String getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        SysConfig config = configMapper.selectByKey(key);
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return defaultValue;
        }
        return config.getConfigValue();
    }

    @Override
    public int getIntValue(String key, int defaultValue) {
        String value = getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean messagePushEnabled() {
        String value = getValue("message_push_enabled", "true");
        return !"false".equalsIgnoreCase(value);
    }

    @Override
    public int attachmentMaxSizeMb() {
        return getIntValue("attachment_max_size", 20);
    }

    @Override
    public List<SysConfig> all() {
        return configMapper.selectAll();
    }

    @Override
    public void update(String configKey, String configValue, String description) {
        if (!StringUtils.hasText(configKey) || !StringUtils.hasText(configValue)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置项与值不能为空");
        }
        if (configMapper.selectByKey(configKey) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配置项不存在: " + configKey);
        }
        configMapper.updateValue(configKey, configValue);
    }
}