package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 文件历史版本 */
@Data
public class DocFileVersionVO {

    private Long versionId;
    private Long fileId;
    private Integer versionNo;
    private String originalName;
    private Long size;
    private String uploaderName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String downloadUrl;
}