package com.tianyu.oasystem.vo;

import lombok.Data;

/** 附件下载信息视图 */
@Data
public class AttachmentVO {

    private Long fileId;
    private String originalName;
    private Long size;
    private String downloadUrl;
}