package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.entity.SysMessage;
import com.tianyu.oasystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 消息中心 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<SysMessage>> page(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) Integer msgType,
                                               @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.myPage(msgType, isRead, pageNum, pageSize));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable("id") Long id) {
        messageService.markRead(id);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll() {
        messageService.markAllRead();
        return Result.success("已全部标为已读", null);
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount() {
        return Result.success(messageService.unreadCount());
    }
}