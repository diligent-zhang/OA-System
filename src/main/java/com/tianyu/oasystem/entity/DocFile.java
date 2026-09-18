package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocFile {

    private Long fileId;
    private Long folderId;
    private String name;
    private String originalName;
    private String storedName;
    private String path;
    private String ext;
    private Long size;
    private String contentType;
    private Long uploaderId;
    private String uploaderName;
    private Integer versionNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    private Integer deleted;
}