package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.entity.SysMessage;
import com.tianyu.oasystem.mapper.SysMessageMapper;
import com.tianyu.oasystem.service.ConfigService;
import com.tianyu.oasystem.service.MessageService;
import com.tianyu.oasystem.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper messageMapper;
    private final ConfigService configService;

    @Override
    public void send(Long receiverUserId, int msgType, String title, String content, String bizLink) {
        if (!configService.messagePushEnabled() || receiverUserId == null) {
            return;
        }
        SysMessage message = new SysMessage();
        message.setReceiverUserId(receiverUserId);
        message.setMsgType(msgType);
        message.setTitle(title);
        message.setContent(content);
        message.setBizLink(bizLink);
        message.setIsRead(0);
        messageMapper.insert(message);
    }

    @Override
    public void sendBatch(Collection<Long> receiverUserIds, int msgType, String title, String content, String bizLink) {
        if (receiverUserIds == null) {
            return;
        }
        receiverUserIds.forEach(id -> send(id, msgType, title, content, bizLink));
    }

    @Override
    public PageResult<SysMessage> myPage(Integer msgType, Integer isRead, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysMessage> list = messageMapper.selectPage(UserContext.userId(), msgType, isRead);
        return PageResult.of(list);
    }

    @Override
    public int unreadCount() {
        return messageMapper.countUnread(UserContext.userId());
    }

    @Override
    public void markRead(Long msgId) {
        int rows = messageMapper.markRead(msgId, UserContext.userId());
        if (rows == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息不存在或无权操作");
        }
    }

    @Override
    public void markAllRead() {
        messageMapper.markAllRead(UserContext.userId());
    }
}