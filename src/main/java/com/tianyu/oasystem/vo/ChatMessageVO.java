package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 聊天消息视图 */
@Data
public class ChatMessageVO {

    private Long msgId;
    private Long conversationId;
    private Long senderUserId;
    private String senderName;
    private String msgType;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
