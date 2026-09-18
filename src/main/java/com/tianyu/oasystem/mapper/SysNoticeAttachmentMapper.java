package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysNoticeAttachment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysNoticeAttachmentMapper {

    int insert(SysNoticeAttachment na);

    List<SysNoticeAttachment> selectByNoticeId(@Param("noticeId") Long noticeId);

    SysNoticeAttachment selectById(@Param("id") Long id);

    int deleteById(@Param("id") Long id);

    int deleteByNoticeId(@Param("noticeId") Long noticeId);
}