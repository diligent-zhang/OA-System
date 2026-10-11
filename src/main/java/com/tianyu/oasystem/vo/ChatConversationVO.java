package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 会话列表项 */
@Data
public class ChatConversationVO {

    private Long conversationId;
    /** SINGLE / GROUP */
    private String type;
    private String groupName;
    /** 单聊=对方姓名，群聊=群名称 */
    private String displayName;
    /** 单聊对方用户ID（用于头像），群聊为 null */
    private Long peerUserId;
    private String lastContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTime;

    private Integer unreadCount;
}
