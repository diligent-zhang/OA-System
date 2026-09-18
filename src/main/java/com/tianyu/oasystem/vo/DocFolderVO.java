package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 文档目录树节点 */
@Data
public class DocFolderVO {

    private Long folderId;
    private Long parentId;
    private String name;
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Boolean canRead;
    private Boolean canEdit;
    private List<DocFolderVO> children = new ArrayList<>();
}