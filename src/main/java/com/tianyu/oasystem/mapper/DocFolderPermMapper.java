package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.DocFolderPerm;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocFolderPermMapper {

    int insert(DocFolderPerm perm);

    List<DocFolderPerm> selectByFolder(@Param("folderId") Long folderId);

    int deleteByFolder(@Param("folderId") Long folderId);

    int delete(@Param("folderId") Long folderId, @Param("permType") String permType,
               @Param("grantType") String grantType, @Param("grantId") Long grantId);
}