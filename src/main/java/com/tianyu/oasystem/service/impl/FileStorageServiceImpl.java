package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${oa.upload-dir:./upload}")
    private String baseDir;

    private String sanitize(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String extension(String originalName) {
        if (!StringUtils.hasText(originalName)) {
            return "";
        }
        int idx = originalName.lastIndexOf('.');
        return idx >= 0 && idx < originalName.length() - 1 ? originalName.substring(idx + 1) : "";
    }

    @Override
    public StoredFile store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件为空");
        }
        String originalName = sanitize(file.getOriginalFilename());
        String ext = extension(originalName);
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 必须使用绝对路径：MultipartFile.transferTo 会把相对路径解析到 Tomcat 临时工作目录
        File dir = new File(base(), subDir + File.separator + month);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(ResultCode.ERROR, "文件目录创建失败");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        File target = new File(dir, storedName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ResultCode.ERROR, "文件保存失败");
        }
        String relativePath = subDir + "/" + month + "/" + storedName;
        String contentType = file.getContentType();
        return new StoredFile(relativePath, storedName, file.getSize(), contentType == null ? "" : contentType);
    }

    @Override
    public File load(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return null;
        }
        File file = new File(base(), relativePath);
        return file.exists() && file.isFile() ? file : null;
    }

    @Override
    public void delete(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return;
        }
        File file = new File(base(), relativePath);
        if (file.exists() && !file.delete()) {
            log.warn("文件删除失败: {}", relativePath);
        }
    }

    private File base() {
        return new File(baseDir).getAbsoluteFile();
    }
}