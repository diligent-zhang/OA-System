package com.tianyu.oasystem.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/** 本地磁盘文件存储：统一管理上传/读取/删除与文件名生成 */
public interface FileStorageService {

    /** 保存上传文件到 {subDir}/{yyyyMM}/{uuid.ext}，返回相对路径等元信息 */
    StoredFile store(MultipartFile file, String subDir);

    /** 根据相对路径加载文件 */
    File load(String relativePath);

    /** 删除相对路径文件 */
    void delete(String relativePath);

    record StoredFile(String relativePath, String storedName, long size, String contentType) {
    }
}