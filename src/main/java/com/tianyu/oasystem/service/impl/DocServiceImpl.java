package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.DocFolderPermDTO;
import com.tianyu.oasystem.entity.DocFile;
import com.tianyu.oasystem.entity.DocFileVersion;
import com.tianyu.oasystem.entity.DocFolder;
import com.tianyu.oasystem.entity.DocFolderPerm;
import com.tianyu.oasystem.mapper.DocFileMapper;
import com.tianyu.oasystem.mapper.DocFileVersionMapper;
import com.tianyu.oasystem.mapper.DocFolderMapper;
import com.tianyu.oasystem.mapper.DocFolderPermMapper;
import com.tianyu.oasystem.service.ConfigService;
import com.tianyu.oasystem.service.DocService;
import com.tianyu.oasystem.service.FileStorageService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.util.UserPrincipal;
import com.tianyu.oasystem.vo.DocFileVersionVO;
import com.tianyu.oasystem.vo.DocFileVO;
import com.tianyu.oasystem.vo.DocFolderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocFolderMapper folderMapper;
    private final DocFolderPermMapper folderPermMapper;
    private final DocFileMapper fileMapper;
    private final DocFileVersionMapper versionMapper;
    private final FileStorageService fileStorageService;
    private final ConfigService configService;

    // ==================== 目录 ====================

    @Override
    public List<DocFolderVO> tree() {
        List<DocFolder> all = folderMapper.selectAll();
        Map<Long, DocFolderVO> nodes = all.stream().collect(Collectors.toMap(
                DocFolder::getFolderId, this::toVO, (a, b) -> a));
        List<DocFolderVO> roots = new ArrayList<>();
        for (DocFolder folder : all) {
            DocFolderVO node = nodes.get(folder.getFolderId());
            if (folder.getParentId() == null || folder.getParentId() == 0) {
                roots.add(node);
            } else {
                DocFolderVO parent = nodes.get(folder.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(Long parentId, String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "目录名称不能为空");
        }
        if (parentId == null || parentId == 0) {
            requireEdit(null);
        } else {
            DocFolder parent = requireFolder(parentId);
            requireEdit(parent.getFolderId());
            if (parent.getParentId() == null || parent.getParentId() == 0) {
                // 允许一级目录之间互建（同层）
            }
        }
        DocFolder folder = new DocFolder();
        folder.setParentId(parentId == null ? 0 : parentId);
        folder.setName(name);
        folder.setCreateBy(UserContext.userId());
        folderMapper.insert(folder);
        return folder.getFolderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFolder(Long folderId, String name) {
        DocFolder folder = requireFolder(folderId);
        requireEdit(folderId);
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "目录名称不能为空");
        }
        folderMapper.updateName(folderId, name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId) {
        DocFolder folder = requireFolder(folderId);
        requireEdit(folderId);
        if (folder.getParentId() == null || folder.getParentId() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "一级目录不允许删除");
        }
        if (folderMapper.countChildren(folderId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "目录下存在子目录，请先清理");
        }
        if (folderMapper.countFiles(folderId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "目录下有文件，请先清空");
        }
        folderPermMapper.deleteByFolder(folderId);
        folderMapper.deleteById(folderId);
    }

    // ==================== 权限 ====================

    @Override
    public List<DocFolderPerm> folderPerms(Long folderId) {
        requireFolder(folderId);
        requireEdit(folderId);
        return folderPermMapper.selectByFolder(folderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantPerm(Long folderId, DocFolderPermDTO dto) {
        requireFolder(folderId);
        requireEdit(folderId);
        DocFolderPerm perm = new DocFolderPerm();
        perm.setFolderId(folderId);
        perm.setPermType(dto.getPermType());
        perm.setGrantType(dto.getGrantType());
        perm.setGrantId(dto.getGrantId());
        boolean exists = folderPermMapper.selectByFolder(folderId).stream()
                .anyMatch(p -> p.getPermType().equals(perm.getPermType())
                        && p.getGrantType().equals(perm.getGrantType())
                        && p.getGrantId().equals(perm.getGrantId()));
        if (!exists) {
            folderPermMapper.insert(perm);
        }
    }

    @Override
    public void revokePerm(Long folderId, DocFolderPermDTO dto) {
        requireFolder(folderId);
        requireEdit(folderId);
        folderPermMapper.delete(folderId, dto.getPermType(), dto.getGrantType(), dto.getGrantId());
    }

    // ==================== 文件 ====================

    @Override
    public List<DocFileVO> files(Long folderId) {
        requireFolder(folderId);
        requireRead(folderId);
        return fileMapper.selectByFolder(folderId).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<DocFileVO> search(String name, int pageNum, int pageSize) {
        String keyword = StringUtils.hasText(name) ? name : "";
        PageHelper.startPage(pageNum, pageSize);
        List<DocFileVO> list = fileMapper.searchByName(keyword).stream()
                .filter(f -> canRead(f.getFolderId()))
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadFile(Long folderId, MultipartFile file, boolean overwrite) {
        requireFolder(folderId);
        requireEdit(folderId);
        checkSize(file);
        String displayName = sanitize(file.getOriginalFilename());
        if (!StringUtils.hasText(displayName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件名不能为空");
        }
        DocFile exist = fileMapper.selectByFolder(folderId).stream()
                .filter(f -> f.getName().equals(displayName))
                .findFirst()
                .orElse(null);
        if (exist != null) {
            if (!overwrite) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "同名文件已存在，可勾选“覆盖上传”以保留历史版本");
            }
            return overwriteFile(exist.getFileId(), file);
        }
        FileStorageService.StoredFile stored = fileStorageService.store(file, "doc");
        DocFile docFile = new DocFile();
        docFile.setFolderId(folderId);
        docFile.setName(displayName);
        docFile.setOriginalName(file.getOriginalFilename());
        docFile.setStoredName(stored.storedName());
        docFile.setPath(stored.relativePath());
        docFile.setExt(extension(file.getOriginalFilename()));
        docFile.setSize(stored.size());
        docFile.setContentType(stored.contentType());
        docFile.setUploaderId(UserContext.userId());
        docFile.setUploaderName(loadUserName());
        fileMapper.insert(docFile);
        return docFile.getFileId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameFile(Long fileId, String name) {
        DocFile file = requireFile(fileId);
        requireEdit(file.getFolderId());
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件名不能为空");
        }
        String sanitized = sanitize(name);
        boolean dup = fileMapper.selectByFolder(file.getFolderId()).stream()
                .anyMatch(f -> !f.getFileId().equals(fileId) && f.getName().equals(sanitized));
        if (dup) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "目录下已存在同名文件");
        }
        fileMapper.updateName(fileId, sanitized);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId) {
        DocFile file = requireFile(fileId);
        requireEdit(file.getFolderId());
        fileStorageService.delete(file.getPath());
        fileMapper.markDeleted(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long overwriteFile(Long fileId, MultipartFile file) {
        DocFile exist = requireFile(fileId);
        requireEdit(exist.getFolderId());
        checkSize(file);

        // 旧内容移入版本表，保留历史
        DocFileVersion version = new DocFileVersion();
        version.setFileId(fileId);
        version.setVersionNo(exist.getVersionNum());
        version.setOriginalName(exist.getOriginalName());
        version.setStoredName(exist.getStoredName());
        version.setPath(exist.getPath());
        version.setSize(exist.getSize());
        version.setUploaderId(exist.getUploaderId());
        version.setUploaderName(exist.getUploaderName());
        versionMapper.insert(version);

        FileStorageService.StoredFile stored = fileStorageService.store(file, "doc");
        fileMapper.updateVersion(fileId, file.getOriginalFilename(), stored.storedName(),
                stored.relativePath(), stored.size(), stored.contentType(), exist.getVersionNum() + 1);
        return fileId;
    }

    @Override
    public List<DocFileVersionVO> versions(Long fileId) {
        DocFile file = requireReadableFile(fileId);
        List<DocFileVersionVO> list = new ArrayList<>();
        // 当前版本置于首位
        DocFileVersionVO current = new DocFileVersionVO();
        current.setFileId(file.getFileId());
        current.setVersionNo(file.getVersionNum());
        current.setOriginalName(file.getOriginalName());
        current.setSize(file.getSize());
        current.setUploaderName(file.getUploaderName());
        current.setUpdateTime(file.getUploadTime());
        current.setDownloadUrl("/api/doc/files/" + file.getFileId() + "/download");
        list.add(current);
        // 历史版本（已归档）
        list.addAll(versionMapper.selectByFile(fileId).stream().map(v -> {
            DocFileVersionVO vo = new DocFileVersionVO();
            vo.setVersionId(v.getVersionId());
            vo.setFileId(v.getFileId());
            vo.setVersionNo(v.getVersionNo());
            vo.setOriginalName(v.getOriginalName());
            vo.setSize(v.getSize());
            vo.setUploaderName(v.getUploaderName());
            vo.setUpdateTime(v.getUpdateTime());
            vo.setDownloadUrl("/api/doc/versions/" + v.getVersionId() + "/download");
            return vo;
        }).collect(Collectors.toList()));
        return list;
    }

    @Override
    public DocFile requireReadableFile(Long fileId) {
        DocFile file = requireFile(fileId);
        requireRead(file.getFolderId());
        return file;
    }

    @Override
    public DocFileVersion requireReadableVersion(Long versionId) {
        DocFileVersion version = versionMapper.selectById(versionId);
        if (version == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件版本不存在");
        }
        DocFile file = requireFile(version.getFileId());
        requireRead(file.getFolderId());
        return version;
    }

    // ==================== 权限判定 ====================

    /** READ/EDIT 检查：沿目录链向上累计显式授权；无任何显式授权时根目录默认全员可读、仅管理员可编辑 */
    private boolean canRead(Long folderId) {
        return checkPerm(folderId, "READ");
    }

    private boolean canEdit(Long folderId) {
        return checkPerm(folderId, "EDIT");
    }

    private boolean checkPerm(Long folderId, String permType) {
        UserPrincipal me = UserContext.get();
        if (me == null || me.isAdmin()) {
            return true;
        }
        boolean anyExplicit = false;
        Long cur = folderId;
        while (cur != null && cur != 0) {
            List<DocFolderPerm> perms = folderPermMapper.selectByFolder(cur);
            if (!perms.isEmpty()) {
                anyExplicit = true;
                for (DocFolderPerm p : perms) {
                    if (p.getPermType().equals(permType) && grantMatches(p, me)) {
                        return true;
                    }
                }
            }
            DocFolder folder = folderMapper.selectById(cur);
            if (folder == null) {
                break;
            }
            cur = (folder.getParentId() == null || folder.getParentId() == 0) ? null : folder.getParentId();
        }
        if (permType.equals("READ")) {
            return !anyExplicit;
        }
        // EDIT：创建者兜底
        Long creator = creatorOf(folderId);
        return creator != null && creator.equals(me.getUserId());
    }

    private boolean grantMatches(DocFolderPerm perm, UserPrincipal me) {
        if ("USER".equals(perm.getGrantType())) {
            return perm.getGrantId().equals(me.getUserId());
        }
        if ("DEPT".equals(perm.getGrantType())) {
            return me.getDeptId() != null && perm.getGrantId().equals(me.getDeptId());
        }
        return false;
    }

    private Long creatorOf(Long folderId) {
        Long cur = folderId;
        while (cur != null && cur != 0) {
            DocFolder folder = folderMapper.selectById(cur);
            if (folder == null) {
                return null;
            }
            if (folder.getCreateBy() != null) {
                return folder.getCreateBy();
            }
            cur = (folder.getParentId() == null || folder.getParentId() == 0) ? null : folder.getParentId();
        }
        return null;
    }

    private void requireRead(Long folderId) {
        if (!canRead(folderId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无该目录的阅读权限");
        }
    }

    private void requireEdit(Long folderId) {
        if (!canEdit(folderId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无该目录的编辑权限");
        }
    }

    // ==================== 工具 ====================

    private void checkSize(MultipartFile file) {
        long maxBytes = (long) configService.attachmentMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "文件大小超过限制（≤ " + configService.attachmentMaxSizeMb() + "MB）");
        }
    }

    private String sanitize(String name) {
        return name == null ? "" : name.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
    }

    private String extension(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        return idx >= 0 && idx < name.length() - 1 ? name.substring(idx + 1) : "";
    }

    private String loadUserName() {
        UserPrincipal me = UserContext.get();
        return me == null ? "" : me.getUsername();
    }

    private DocFolder requireFolder(Long folderId) {
        DocFolder folder = folderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目录不存在");
        }
        return folder;
    }

    private DocFile requireFile(Long fileId) {
        DocFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已删除");
        }
        return file;
    }

    private DocFolderVO toVO(DocFolder folder) {
        DocFolderVO vo = new DocFolderVO();
        vo.setFolderId(folder.getFolderId());
        vo.setParentId(folder.getParentId());
        vo.setName(folder.getName());
        vo.setCreateBy(folder.getCreateBy());
        vo.setCreateTime(folder.getCreateTime());
        vo.setCanRead(canRead(folder.getFolderId()));
        vo.setCanEdit(canEdit(folder.getFolderId()));
        return vo;
    }

    private DocFileVO toVO(DocFile file) {
        DocFileVO vo = new DocFileVO();
        vo.setFileId(file.getFileId());
        vo.setFolderId(file.getFolderId());
        vo.setName(file.getName());
        vo.setOriginalName(file.getOriginalName());
        vo.setExt(file.getExt());
        vo.setSize(file.getSize());
        vo.setUploaderName(file.getUploaderName());
        vo.setVersionNum(file.getVersionNum());
        vo.setContentType(file.getContentType());
        vo.setUploadTime(file.getUploadTime());
        vo.setDownloadUrl("/api/doc/files/" + file.getFileId() + "/download");
        return vo;
    }
}