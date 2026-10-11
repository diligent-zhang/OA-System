package com.tianyu.oasystem.dto;

import lombok.Data;

/** 发送消息请求 */
@Data
public class ChatSendDTO {

    private Long conversationId;
    private String content;
}
