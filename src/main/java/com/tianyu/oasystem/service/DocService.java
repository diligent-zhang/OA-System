package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.dto.DocFolderPermDTO;
import com.tianyu.oasystem.entity.DocFile;
import com.tianyu.oasystem.entity.DocFileVersion;
import com.tianyu.oasystem.entity.DocFolderPerm;
import com.tianyu.oasystem.vo.DocFileVersionVO;
import com.tianyu.oasystem.vo.DocFileVO;
import com.tianyu.oasystem.vo.DocFolderVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 文档中心：目录 / 权限 / 文件 / 版本 */
public interface DocService {

    // ==================== 目录 ====================

    List<DocFolderVO> tree();

    Long createFolder(Long parentId, String name);

    void renameFolder(Long folderId, String name);

    void deleteFolder(Long folderId);

    // ==================== 权限 ====================

    List<DocFolderPerm> folderPerms(Long folderId);

    void grantPerm(Long folderId, DocFolderPermDTO dto);

    void revokePerm(Long folderId, DocFolderPermDTO dto);

    // ==================== 文件 ====================

    List<DocFileVO> files(Long folderId);

    PageResult<DocFileVO> search(String name, int pageNum, int pageSize);

    /** 上传；同名存在且 overwrite=true 时覆盖（保留历史版本） */
    Long uploadFile(Long folderId, MultipartFile file, boolean overwrite);

    void renameFile(Long fileId, String name);

    void deleteFile(Long fileId);

    /** 覆盖上传形成新版本 */
    Long overwriteFile(Long fileId, MultipartFile file);

    List<DocFileVersionVO> versions(Long fileId);

    DocFile requireReadableFile(Long fileId);

    DocFileVersion requireReadableVersion(Long versionId);
}