package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 聊天消息 */
@Data
public class ChatMessage {

    private Long msgId;
    private Long conversationId;
    private Long senderUserId;
    private String msgType;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
