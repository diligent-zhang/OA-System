package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.DocFolder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocFolderMapper {

    DocFolder selectById(@Param("folderId") Long folderId);

    List<DocFolder> selectAll();

    int countChildren(@Param("parentId") Long parentId);

    int countFiles(@Param("folderId") Long folderId);

    int insert(DocFolder folder);

    int updateName(@Param("folderId") Long folderId, @Param("name") String name);

    int deleteById(@Param("folderId") Long folderId);
}