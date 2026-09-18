package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.DocFolderPermDTO;
import com.tianyu.oasystem.entity.DocFile;
import com.tianyu.oasystem.entity.DocFileVersion;
import com.tianyu.oasystem.entity.DocFolderPerm;
import com.tianyu.oasystem.service.DocService;
import com.tianyu.oasystem.service.FileStorageService;
import com.tianyu.oasystem.vo.DocFileVersionVO;
import com.tianyu.oasystem.vo.DocFileVO;
import com.tianyu.oasystem.vo.DocFolderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 文档中心：目录 / 权限 / 文件 / 版本 / 检索 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocController {

    private static final Set<String> PREVIEW_EXTS =
            Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf");

    private final DocService docService;
    private final FileStorageService fileStorageService;

    // ==================== 目录 ====================

    @GetMapping("/folders/tree")
    public Result<List<DocFolderVO>> tree() {
        return Result.success(docService.tree());
    }

    @PostMapping("/folders")
    @OpLog(type = "DOC", biz = "文档目录", op = "新建")
    public Result<Long> createFolder(@RequestParam Long parentId, @RequestParam String name) {
        return Result.success("目录创建成功", docService.createFolder(parentId, name));
    }

    @PutMapping("/folders/{id}/rename")
    @OpLog(type = "DOC", biz = "文档目录", op = "重命名")
    public Result<Void> renameFolder(@PathVariable("id") Long id, @RequestParam String name) {
        docService.renameFolder(id, name);
        return Result.success("目录已重命名", null);
    }

    @DeleteMapping("/folders/{id}")
    @OpLog(type = "DOC", biz = "文档目录", op = "删除")
    public Result<Void> deleteFolder(@PathVariable("id") Long id) {
        docService.deleteFolder(id);
        return Result.success("目录已删除", null);
    }

    // ==================== 权限 ====================

    @GetMapping("/folders/{id}/perms")
    public Result<List<DocFolderPerm>> perms(@PathVariable("id") Long id) {
        return Result.success(docService.folderPerms(id));
    }

    @PostMapping("/folders/{id}/perms")
    @OpLog(type = "DOC", biz = "文档权限", op = "授权")
    public Result<Void> grantPerm(@PathVariable("id") Long id, @Valid @RequestBody DocFolderPermDTO dto) {
        docService.grantPerm(id, dto);
        return Result.success("授权成功", null);
    }

    @DeleteMapping("/folders/{id}/perms")
    @OpLog(type = "DOC", biz = "文档权限", op = "取消授权")
    public Result<Void> revokePerm(@PathVariable("id") Long id, @Valid @RequestBody DocFolderPermDTO dto) {
        docService.revokePerm(id, dto);
        return Result.success("授权已取消", null);
    }

    // ==================== 文件 ====================

    @GetMapping("/folders/{id}/files")
    public Result<List<DocFileVO>> files(@PathVariable("id") Long id) {
        return Result.success(docService.files(id));
    }

    @GetMapping("/files/search")
    public Result<PageResult<DocFileVO>> search(@RequestParam String name,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(docService.search(name, pageNum, pageSize));
    }

    @PostMapping("/folders/{id}/files/upload")
    @OpLog(type = "DOC", biz = "文档文件", op = "上传")
    public Result<Long> upload(@PathVariable("id") Long folderId,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(defaultValue = "false") boolean overwrite) {
        return Result.success("上传成功", docService.uploadFile(folderId, file, overwrite));
    }

    @PostMapping("/files/{fileId}/version")
    @OpLog(type = "DOC", biz = "文档文件", op = "覆盖上传")
    public Result<Long> overwrite(@PathVariable("fileId") Long fileId,
                                  @RequestParam("file") MultipartFile file) {
        return Result.success("覆盖成功，历史版本已保留", docService.overwriteFile(fileId, file));
    }

    @PutMapping("/files/{fileId}/rename")
    @OpLog(type = "DOC", biz = "文档文件", op = "重命名")
    public Result<Void> renameFile(@PathVariable("fileId") Long fileId, @RequestParam String name) {
        docService.renameFile(fileId, name);
        return Result.success("文件已重命名", null);
    }

    @DeleteMapping("/files/{fileId}")
    @OpLog(type = "DOC", biz = "文档文件", op = "删除")
    public Result<Void> deleteFile(@PathVariable("fileId") Long fileId) {
        docService.deleteFile(fileId);
        return Result.success("文件已删除", null);
    }

    @GetMapping("/files/{fileId}/versions")
    public Result<List<DocFileVersionVO>> versions(@PathVariable("fileId") Long fileId) {
        return Result.success(docService.versions(fileId));
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable("fileId") Long fileId) {
        DocFile file = docService.requireReadableFile(fileId);
        File disk = fileStorageService.load(file.getPath());
        boolean inline = isPreview(file.getExt());
        return buildResponse(disk, file.getOriginalName(), file.getContentType(),
                inline, file.getExt());
    }

    @GetMapping("/versions/{versionId}/download")
    public ResponseEntity<Resource> downloadVersion(@PathVariable("versionId") Long versionId) {
        DocFileVersion version = docService.requireReadableVersion(versionId);
        File disk = fileStorageService.load(version.getPath());
        String ext = extension(version.getOriginalName());
        return buildResponse(disk, version.getOriginalName(), mediaTypeFor(ext), false, ext);
    }

    // ==================== 工具 ====================

    private boolean isPreview(String ext) {
        return ext != null && PREVIEW_EXTS.contains(ext.toLowerCase(Locale.ROOT));
    }

    private String extension(String name) {
        if (name == null) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        return idx >= 0 && idx < name.length() - 1 ? name.substring(idx + 1) : "";
    }

    private String mediaTypeFor(String ext) {
        return switch (ext == null ? "" : ext.toLowerCase(Locale.ROOT)) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt", "md", "csv" -> "text/plain; charset=utf-8";
            default -> "application/octet-stream";
        };
    }

    private ResponseEntity<Resource> buildResponse(File file, String displayName,
                                                   String contentType, boolean inline, String ext) {
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        MediaType mediaType = contentType != null && !contentType.isBlank()
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
        String encoded = URLEncoder.encode(displayName, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = (inline ? "inline; " : "attachment; ") + "filename*=UTF-8''" + encoded;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(mediaType)
                .contentLength(file.length())
                .body(resource);
    }
}