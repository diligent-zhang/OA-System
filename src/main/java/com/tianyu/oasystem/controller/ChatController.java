package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.ChatCreateDTO;
import com.tianyu.oasystem.dto.ChatSendDTO;
import com.tianyu.oasystem.service.ChatService;
import com.tianyu.oasystem.vo.ChatConversationVO;
import com.tianyu.oasystem.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 即时通讯：会话列表 / 历史消息 / 发送（发送后经 WebSocket 实时推送） */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> conversations() {
        return Result.success(chatService.myConversations());
    }

    /** 打开会话（单聊复用已有会话，群聊新建），返回 conversationId */
    @PostMapping("/conversations")
    public Result<Long> open(@RequestBody ChatCreateDTO dto) {
        return Result.success(chatService.openConversation(dto));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<ChatMessageVO>> messages(@PathVariable("id") Long id,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "30") int pageSize) {
        return Result.success(chatService.messages(id, pageNum, pageSize));
    }

    @PutMapping("/conversations/{id}/read")
    public Result<Void> read(@PathVariable("id") Long id) {
        chatService.markRead(id);
        return Result.success();
    }

    @PostMapping("/messages")
    public Result<ChatMessageVO> send(@RequestBody ChatSendDTO dto) {
        return Result.success(chatService.send(dto.getConversationId(), dto.getContent()));
    }
}
