package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.ChatConversation;
import com.tianyu.oasystem.vo.ChatConversationVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatConversationMapper {

    int insert(ChatConversation conversation);

    ChatConversation selectById(@Param("conversationId") Long conversationId);

    ChatConversation selectBySingleKey(@Param("singleKey") String singleKey);

    int updateLast(@Param("conversationId") Long conversationId,
                   @Param("lastMsgId") Long lastMsgId,
                   @Param("lastTime") LocalDateTime lastTime);

    /** 我参与的会话列表（含最后一条消息与未读数） */
    List<ChatConversationVO> selectByUser(@Param("userId") Long userId);
}
