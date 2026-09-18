package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 文档文件列表项 */
@Data
public class DocFileVO {

    private Long fileId;
    private Long folderId;
    private String name;
    private String originalName;
    private String ext;
    private Long size;
    private String uploaderName;
    private Integer versionNum;
    private String contentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    private String downloadUrl;
    private String previewUrl;
}