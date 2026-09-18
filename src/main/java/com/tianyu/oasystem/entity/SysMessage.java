package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMessage {

    private Long msgId;
    private Long receiverUserId;
    private Integer msgType;
    private String title;
    private String content;
    private String bizLink;
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}