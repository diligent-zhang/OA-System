package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysNoticeAttachment {

    private Long id;
    private Long noticeId;
    private Long fileId;
    private String originalName;
    private Long size;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
}