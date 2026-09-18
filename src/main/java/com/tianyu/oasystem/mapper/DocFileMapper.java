package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.DocFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocFileMapper {

    DocFile selectById(@Param("fileId") Long fileId);

    /** 目录文件分页（不含逻辑删除） */
    List<DocFile> selectByFolder(@Param("folderId") Long folderId);

    /** 名称模糊搜索（不含逻辑删除） */
    List<DocFile> searchByName(@Param("name") String name);

    int countByFolderAndName(@Param("folderId") Long folderId, @Param("name") String name);

    int countByFolder(@Param("folderId") Long folderId);

    int insert(DocFile file);

    int updateName(@Param("fileId") Long fileId, @Param("name") String name);

    int updateVersion(@Param("fileId") Long fileId, @Param("originalName") String originalName,
                      @Param("storedName") String storedName, @Param("path") String path,
                      @Param("size") Long size, @Param("contentType") String contentType,
                      @Param("versionNum") Integer versionNum);

    int markDeleted(@Param("fileId") Long fileId);
}