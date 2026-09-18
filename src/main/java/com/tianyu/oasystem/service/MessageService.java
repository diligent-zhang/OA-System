package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.entity.SysMessage;

import java.util.Collection;

public interface MessageService {

    /** 发送单条站内消息（受全局推送开关控制，开关关闭时静默丢弃） */
    void send(Long receiverUserId, int msgType, String title, String content, String bizLink);

    /** 批量发送（广播：公告等） */
    void sendBatch(Collection<Long> receiverUserIds, int msgType, String title, String content, String bizLink);

    /** 我的消息分页 */
    PageResult<SysMessage> myPage(Integer msgType, Integer isRead, int pageNum, int pageSize);

    /** 我的未读数 */
    int unreadCount();

    /** 标记已读（校验归属） */
    void markRead(Long msgId);

    /** 全部已读 */
    void markAllRead();
}