package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.DocFileVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DocFileVersionMapper {

    int insert(DocFileVersion version);

    List<DocFileVersion> selectByFile(@Param("fileId") Long fileId);

    DocFileVersion selectById(@Param("versionId") Long versionId);
}