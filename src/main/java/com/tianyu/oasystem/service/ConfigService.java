package com.tianyu.oasystem.service;

import com.tianyu.oasystem.entity.SysConfig;

import java.util.List;

public interface ConfigService {

    /** 读取配置值，缺省返回默认值 */
    String getValue(String key);

    String getValue(String key, String defaultValue);

    int getIntValue(String key, int defaultValue);

    /** 消息推送总开关 */
    boolean messagePushEnabled();

    /** 附件/头像大小上限(MB) */
    int attachmentMaxSizeMb();

    List<SysConfig> all();

    void update(String configKey, String configValue, String description);
}