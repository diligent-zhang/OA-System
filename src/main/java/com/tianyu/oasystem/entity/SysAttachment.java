package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysAttachment {

    private Long fileId;
    private String bizType;
    private String bizId;
    private String originalName;
    private String storedName;
    private String path;
    private Long size;
    private String contentType;
    private Long uploaderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
}