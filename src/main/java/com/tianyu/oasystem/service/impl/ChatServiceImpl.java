package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.ChatCreateDTO;
import com.tianyu.oasystem.entity.ChatConversation;
import com.tianyu.oasystem.entity.ChatConversationMember;
import com.tianyu.oasystem.entity.ChatMessage;
import com.tianyu.oasystem.entity.EmpEmployee;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.ChatConversationMapper;
import com.tianyu.oasystem.mapper.ChatConversationMemberMapper;
import com.tianyu.oasystem.mapper.ChatMessageMapper;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.ChatService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.ChatConversationVO;
import com.tianyu.oasystem.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String TYPE_SINGLE = "SINGLE";
    private static final String TYPE_GROUP = "GROUP";
    private static final int MAX_CONTENT = 2000;

    private final ChatConversationMapper conversationMapper;
    private final ChatConversationMemberMapper memberMapper;
    private final ChatMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<ChatConversationVO> myConversations() {
        Long me = UserContext.userId();
        List<ChatConversationVO> list = conversationMapper.selectByUser(me);
        for (ChatConversationVO vo : list) {
            if (TYPE_SINGLE.equals(vo.getType())) {
                Long peer = peerUserId(vo.getConversationId(), me);
                vo.setPeerUserId(peer);
                vo.setDisplayName(peer == null ? "会话" : userName(peer));
            } else {
                vo.setDisplayName(StringUtils.hasText(vo.getGroupName()) ? vo.getGroupName() : "群聊");
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long openConversation(ChatCreateDTO dto) {
        Long me = UserContext.userId();
        List<Long> targets = (dto == null || dto.getMemberUserIds() == null)
                ? new ArrayList<>()
                : dto.getMemberUserIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !id.equals(me))
                .collect(Collectors.toList());
        if (targets.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择会话成员");
        }
        boolean group = TYPE_GROUP.equalsIgnoreCase(dto.getType());
        if (!group) {
            if (targets.size() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "单聊只能选择一位成员");
            }
            Long other = targets.get(0);
            requireActiveUser(other);
            String key = singleKey(me, other);
            ChatConversation exist = conversationMapper.selectBySingleKey(key);
            if (exist != null) {
                return exist.getConversationId();
            }
            ChatConversation conv = new ChatConversation();
            conv.setType(TYPE_SINGLE);
            conv.setSingleKey(key);
            conv.setCreateBy(me);
            conversationMapper.insert(conv);
            addMember(conv.getConversationId(), me);
            addMember(conv.getConversationId(), other);
            return conv.getConversationId();
        }

        List<Long> all = new ArrayList<>();
        all.add(me);
        all.addAll(targets);
        all.forEach(this::requireActiveUser);
        String groupName = StringUtils.hasText(dto.getGroupName())
                ? dto.getGroupName().trim()
                : "群聊（" + all.size() + "人）";
        ChatConversation conv = new ChatConversation();
        conv.setType(TYPE_GROUP);
        conv.setGroupName(groupName);
        conv.setCreateBy(me);
        conversationMapper.insert(conv);
        all.forEach(uid -> addMember(conv.getConversationId(), uid));
        return conv.getConversationId();
    }

    @Override
    public List<ChatMessageVO> messages(Long conversationId, int pageNum, int pageSize) {
        Long me = UserContext.userId();
        requireMember(conversationId, me);
        PageHelper.startPage(Math.max(pageNum, 1), Math.max(pageSize, 1));
        List<ChatMessageVO> list = messageMapper.selectPage(conversationId);
        Map<Long, String> nameCache = new HashMap<>();
        for (ChatMessageVO m : list) {
            m.setSenderName(nameCache.computeIfAbsent(m.getSenderUserId(), this::userName));
        }
        Collections.reverse(list); // 倒序翻页，反转为时间正序展示
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO send(Long conversationId, String content) {
        Long me = UserContext.userId();
        ChatConversation conv = requireMember(conversationId, me);
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "消息内容不能为空");
        }
        String text = content.length() > MAX_CONTENT ? content.substring(0, MAX_CONTENT) : content;

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderUserId(me);
        msg.setMsgType("TEXT");
        msg.setContent(text);
        messageMapper.insert(msg);

        LocalDateTime now = LocalDateTime.now();
        conversationMapper.updateLast(conversationId, msg.getMsgId(), now);
        memberMapper.updateLastRead(conversationId, me, msg.getMsgId());

        ChatMessageVO vo = new ChatMessageVO();
        vo.setMsgId(msg.getMsgId());
        vo.setConversationId(conversationId);
        vo.setSenderUserId(me);
        vo.setSenderName(userName(me));
        vo.setMsgType("TEXT");
        vo.setContent(text);
        vo.setCreateTime(now);

        push(conv, vo);
        return vo;
    }

    @Override
    public void markRead(Long conversationId) {
        Long me = UserContext.userId();
        requireMember(conversationId, me);
        Long max = messageMapper.maxMsgId(conversationId);
        memberMapper.updateLastRead(conversationId, me, max == null ? 0L : max);
    }

    // ==================== 内部 ====================

    /** 实时推送：会话频道（当前打开的会话）+ 各成员用户队列（会话列表/未读刷新） */
    private void push(ChatConversation conv, ChatMessageVO vo) {
        try {
            messagingTemplate.convertAndSend("/topic/chat/" + conv.getConversationId(), vo);
            for (ChatConversationMember m : memberMapper.selectByConversation(conv.getConversationId())) {
                messagingTemplate.convertAndSendToUser(String.valueOf(m.getUserId()), "/queue/chat", vo);
            }
        } catch (Exception e) {
            log.warn("聊天消息推送失败, conversationId={}", conv.getConversationId(), e);
        }
    }

    private void addMember(Long conversationId, Long userId) {
        ChatConversationMember m = new ChatConversationMember();
        m.setConversationId(conversationId);
        m.setUserId(userId);
        m.setLastReadMsgId(0L);
        memberMapper.insert(m);
    }

    private ChatConversation requireMember(Long conversationId, Long userId) {
        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (memberMapper.countMember(conversationId, userId) == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return conv;
    }

    private Long peerUserId(Long conversationId, Long selfUserId) {
        return memberMapper.selectByConversation(conversationId).stream()
                .map(ChatConversationMember::getUserId)
                .filter(id -> !id.equals(selfUserId))
                .findFirst()
                .orElse(null);
    }

    private void requireActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "成员账号不存在或已停用");
        }
    }

    private String singleKey(Long a, Long b) {
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min + "-" + max;
    }

    private String userName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return String.valueOf(userId);
        }
        if (user.getEmpId() != null) {
            EmpEmployee emp = employeeMapper.selectById(user.getEmpId());
            if (emp != null && StringUtils.hasText(emp.getName())) {
                return emp.getName();
            }
        }
        return user.getUsername();
    }
}
