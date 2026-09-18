package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysAttachmentMapper {

    int insert(SysAttachment attachment);

    List<SysAttachment> selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);

    SysAttachment selectById(@Param("fileId") Long fileId);

    int deleteById(@Param("fileId") Long fileId);

    /** 上传后业务创建，回填业务ID */
    int updateBizId(@Param("fileId") Long fileId, @Param("bizId") String bizId);
}