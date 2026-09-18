package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.entity.SysAttachment;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.SysAttachmentMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.ConfigService;
import com.tianyu.oasystem.service.FileStorageService;
import com.tianyu.oasystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** 通用附件与头像：上传 / 下载（本地磁盘存储，JWT 鉴权） */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final SysAttachmentMapper attachmentMapper;
    private final SysUserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final ConfigService configService;

    /** 业务附件上传：先上传获得 fileId，再随业务提交 */
    @PostMapping("/file/upload")
    @OpLog(type = "FILE", biz = "附件", op = "上传")
    public Result<SysAttachment> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam String bizType,
                                        @RequestParam String bizId) {
        long maxBytes = (long) configService.attachmentMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "附件大小超过限制（≤ " + configService.attachmentMaxSizeMb() + "MB）");
        }
        if (!StringUtils.hasText(bizType) || !StringUtils.hasText(bizId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "缺少业务类型或业务ID");
        }
        FileStorageService.StoredFile stored = fileStorageService.store(file, "att");
        SysAttachment attachment = new SysAttachment();
        attachment.setBizType(bizType);
        attachment.setBizId(bizId);
        attachment.setOriginalName(file.getOriginalFilename());
        attachment.setStoredName(stored.storedName());
        attachment.setPath(stored.relativePath());
        attachment.setSize(stored.size());
        attachment.setContentType(stored.contentType());
        attachment.setUploaderId(UserContext.userId());
        attachmentMapper.insert(attachment);
        return Result.success(attachment);
    }

    /** 附件下载 */
    @GetMapping("/file/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        SysAttachment attachment = attachmentMapper.selectById(fileId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "附件不存在");
        }
        return buildFileResponse(fileStorageService.load(attachment.getPath()),
                attachment.getOriginalName(), attachment.getContentType(), false);
    }

    /** 头像读取（按用户ID，JWT 鉴权后浏览器可直接展示） */
    @GetMapping("/file/avatar/{userId}")
    public ResponseEntity<Resource> avatar(@PathVariable Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !StringUtils.hasText(user.getAvatarUrl())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "头像不存在");
        }
        File file = fileStorageService.load(user.getAvatarUrl());
        return buildFileResponse(file, "avatar", mediaTypeFor(user.getAvatarUrl()), true);
    }

    private ResponseEntity<Resource> buildFileResponse(File file, String displayName,
                                                       String contentType, boolean inline) {
        if (file == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已被删除");
        }
        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = StringUtils.hasText(contentType) && !"application/octet-stream".equals(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
        String encoded = URLEncoder.encode(displayName, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = (inline ? "inline; " : "attachment; ") + "filename*=UTF-8''" + encoded;
        headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition);
        headers.setContentType(mediaType);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }

    private String mediaTypeFor(String path) {
        if (path == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String ext = path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }
}