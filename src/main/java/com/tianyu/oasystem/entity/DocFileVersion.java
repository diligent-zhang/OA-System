package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocFileVersion {

    private Long versionId;
    private Long fileId;
    private Integer versionNo;
    private String originalName;
    private String storedName;
    private String path;
    private Long size;
    private Long uploaderId;
    private String uploaderName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}