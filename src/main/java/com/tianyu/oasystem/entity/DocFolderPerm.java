package com.tianyu.oasystem.entity;

import lombok.Data;

@Data
public class DocFolderPerm {

    private Long id;
    private Long folderId;
    private String permType;
    private String grantType;
    private Long grantId;
}