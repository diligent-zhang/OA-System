package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.ChatCreateDTO;
import com.tianyu.oasystem.vo.ChatConversationVO;
import com.tianyu.oasystem.vo.ChatMessageVO;

import java.util.List;

/** 即时通讯：会话与消息 */
public interface ChatService {

    /** 我参与的会话列表 */
    List<ChatConversationVO> myConversations();

    /** 打开（新建或复用）会话，返回会话ID */
    Long openConversation(ChatCreateDTO dto);

    /** 会话历史消息（时间正序，pageNum=1 为最新一页） */
    List<ChatMessageVO> messages(Long conversationId, int pageNum, int pageSize);

    /** 发送消息并实时推送 */
    ChatMessageVO send(Long conversationId, String content);

    /** 标记会话已读 */
    void markRead(Long conversationId);
}
