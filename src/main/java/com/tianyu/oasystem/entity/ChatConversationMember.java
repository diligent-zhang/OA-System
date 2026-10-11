package com.tianyu.oasystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 会话成员及其已读位置 */
@Data
public class ChatConversationMember {

    private Long id;
    private Long conversationId;
    private Long userId;
    /** 该成员已读到的最大消息ID，用于计算未读数 */
    private Long lastReadMsgId;
    private LocalDateTime joinTime;
}
