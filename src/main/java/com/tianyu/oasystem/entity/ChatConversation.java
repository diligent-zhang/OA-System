package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 即时通讯会话（单聊/群聊） */
@Data
public class ChatConversation {

    private Long conversationId;
    /** SINGLE 单聊 / GROUP 群聊 */
    private String type;
    private String groupName;
    /** 单聊去重键 minUserId-maxUserId，群聊为 null */
    private String singleKey;
    private Long createBy;
    private Long lastMsgId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
