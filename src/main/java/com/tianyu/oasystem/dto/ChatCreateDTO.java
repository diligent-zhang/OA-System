package com.tianyu.oasystem.dto;

import lombok.Data;

import java.util.List;

/** 创建会话请求：单聊传对方用户ID，群聊传成员列表与群名 */
@Data
public class ChatCreateDTO {

    /** SINGLE 单聊 / GROUP 群聊 */
    private String type;
    /** 除自己外的成员用户ID */
    private List<Long> memberUserIds;
    private String groupName;
}
