package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.ChatConversationMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatConversationMemberMapper {

    int insert(ChatConversationMember member);

    int countMember(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    List<ChatConversationMember> selectByConversation(@Param("conversationId") Long conversationId);

    int updateLastRead(@Param("conversationId") Long conversationId,
                       @Param("userId") Long userId,
                       @Param("msgId") Long msgId);
}
