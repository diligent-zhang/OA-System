package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.ChatMessage;
import com.tianyu.oasystem.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {

    int insert(ChatMessage message);

    /** 会话消息分页（按 msg_id 倒序，service 反转为时间正序） */
    List<ChatMessageVO> selectPage(@Param("conversationId") Long conversationId);

    Long maxMsgId(@Param("conversationId") Long conversationId);
}
