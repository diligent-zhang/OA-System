package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMessageMapper {

    int insert(SysMessage message);

    /** 我的消息分页（msg_type / is_read 筛选） */
    List<SysMessage> selectPage(@Param("receiverUserId") Long receiverUserId,
                                @Param("msgType") Integer msgType,
                                @Param("isRead") Integer isRead);

    int countUnread(@Param("receiverUserId") Long receiverUserId);

    SysMessage selectById(@Param("msgId") Long msgId);

    int markRead(@Param("msgId") Long msgId, @Param("userId") Long userId);

    int markAllRead(@Param("userId") Long userId);
}