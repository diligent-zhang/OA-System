package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.NoticeDTO;
import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.entity.SysAttachment;
import com.tianyu.oasystem.entity.SysNotice;
import com.tianyu.oasystem.entity.SysNoticeAttachment;
import com.tianyu.oasystem.mapper.SysAttachmentMapper;
import com.tianyu.oasystem.mapper.SysNoticeAttachmentMapper;
import com.tianyu.oasystem.mapper.SysNoticeMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.FileStorageService;
import com.tianyu.oasystem.service.MessageService;
import com.tianyu.oasystem.service.NoticeService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.NoticeReadStatsVO;
import com.tianyu.oasystem.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeAttachmentMapper noticeAttachmentMapper;
    private final SysAttachmentMapper attachmentMapper;
    private final SysUserMapper userMapper;
    private final FileStorageService fileStorageService;
    private final MessageService messageService;

    @Override
    public PageResult<NoticeVO> adminPage(NoticeQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return PageResult.of(noticeMapper.selectPageAdmin(query));
    }

    @Override
    public PageResult<NoticeVO> publishedPage(NoticeQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<NoticeVO> list = noticeMapper.selectPagePublished(query);
        fillReadFlags(list);
        return PageResult.of(list);
    }

    @Override
    public NoticeVO detail(Long noticeId) {
        NoticeVO vo = noticeMapper.selectVOById(noticeId);
        if (vo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        // 员工只能查看已发布公告
        if (vo.getStatus() == 0 && !UserContext.get().isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "公告已下架");
        }
        vo.setAttachments(buildAttachments(noticeId));
        if (vo.getStatus() == 1) {
            // 阅读即记录（幂等）；发布人不重复计数由业务侧可忽略
            noticeMapper.insertRead(noticeId, UserContext.userId());
        }
        vo.setReadFlag(true);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(NoticeDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setTopFlag(dto.getTopFlag() == null ? 0 : dto.getTopFlag());
        notice.setStatus(Boolean.FALSE.equals(dto.getPublish()) ? 0 : 1);
        notice.setPublisherId(UserContext.userId());
        noticeMapper.insert(notice);

        bindAttachments(notice.getNoticeId(), dto.getAttachmentFileIds());

        if (notice.getStatus() == 1) {
            broadcastNotice(notice);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long noticeId, NoticeDTO dto) {
        SysNotice exist = requireNotice(noticeId);
        SysNotice notice = new SysNotice();
        notice.setNoticeId(noticeId);
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setTopFlag(dto.getTopFlag() == null ? 0 : dto.getTopFlag());
        // 编辑不改变上下架状态
        notice.setStatus(exist.getStatus());
        notice.setPublisherId(exist.getPublisherId());
        noticeMapper.update(notice);

        bindAttachments(noticeId, dto.getAttachmentFileIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noticeId) {
        requireNotice(noticeId);
        deleteAttachmentsPhysical(noticeId);
        noticeAttachmentMapper.deleteByNoticeId(noticeId);
        noticeMapper.deleteReads(noticeId);
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public void toggleStatus(Long noticeId) {
        SysNotice exist = requireNotice(noticeId);
        int newStatus = exist.getStatus() == 1 ? 0 : 1;
        noticeMapper.updateStatus(noticeId, newStatus);
        if (newStatus == 1) {
            broadcastNotice(exist);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long noticeId, Long attachmentId) {
        requireNotice(noticeId);
        SysNoticeAttachment na = noticeAttachmentMapper.selectById(attachmentId);
        if (na == null || !na.getNoticeId().equals(noticeId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "附件不存在");
        }
        noticeAttachmentMapper.deleteById(attachmentId);
        SysAttachment att = attachmentMapper.selectById(na.getFileId());
        if (att != null) {
            fileStorageService.delete(att.getPath());
            attachmentMapper.deleteById(att.getFileId());
        }
    }

    @Override
    public NoticeReadStatsVO readStats(Long noticeId) {
        requireNotice(noticeId);
        int total = userMapper.countActive();
        int read = noticeMapper.countRead(noticeId);
        List<String> readUsers = noticeMapper.selectReadUsernames(noticeId);
        List<String> allUsers = userMapper.selectAllActiveUsernames();
        List<String> notReadUsers = allUsers.stream()
                .filter(u -> !readUsers.contains(u))
                .collect(Collectors.toList());
        NoticeReadStatsVO vo = new NoticeReadStatsVO();
        vo.setTotalUsers((long) total);
        vo.setReadCount((long) read);
        vo.setNotReadCount((long) total - read);
        vo.setReadUsers(readUsers);
        vo.setNotReadUsers(notReadUsers);
        return vo;
    }

    @Override
    public List<NoticeVO> latest(int limit) {
        return noticeMapper.selectLatest(limit);
    }

    private void fillReadFlags(List<NoticeVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(NoticeVO::getNoticeId).collect(Collectors.toList());
        Set<Long> readIds = new HashSet<>(noticeMapper.selectReadNoticeIds(UserContext.userId(), ids));
        list.forEach(vo -> vo.setReadFlag(readIds.contains(vo.getNoticeId())));
    }

    private List<Object> buildAttachments(Long noticeId) {
        List<SysNoticeAttachment> nas = noticeAttachmentMapper.selectByNoticeId(noticeId);
        if (CollectionUtils.isEmpty(nas)) {
            return new ArrayList<>();
        }
        return nas.stream().map(na -> java.util.Map.of(
                "fileId", na.getFileId(),
                "originalName", na.getOriginalName(),
                "size", na.getSize(),
                "downloadUrl", "/api/file/" + na.getFileId() + "/download"
        )).collect(Collectors.toList());
    }

    /** 绑定附件：将已上传附件（临时 bizId）回填到公告 ID，并写公告附件表 */
    private void bindAttachments(Long noticeId, List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        String bizId = String.valueOf(noticeId);
        for (Long fileId : fileIds) {
            SysAttachment att = attachmentMapper.selectById(fileId);
            if (att == null) {
                continue;
            }
            attachmentMapper.updateBizId(fileId, bizId);
            SysNoticeAttachment na = new SysNoticeAttachment();
            na.setNoticeId(noticeId);
            na.setFileId(fileId);
            na.setOriginalName(att.getOriginalName());
            na.setSize(att.getSize());
            noticeAttachmentMapper.insert(na);
        }
    }

    private void broadcastNotice(SysNotice notice) {
        Long publisher = notice.getPublisherId();
        List<Long> receivers = userMapper.selectAllActiveUserIds().stream()
                .filter(id -> !id.equals(publisher))
                .collect(Collectors.toList());
        if (receivers.isEmpty()) {
            return;
        }
        String title = StringUtils.hasText(notice.getTitle()) ? notice.getTitle() : "公告";
        messageService.sendBatch(receivers, 3, "新公告：" + title,
                notice.getContent(), "/employee/notices?id=" + notice.getNoticeId());
    }

    private void deleteAttachmentsPhysical(Long noticeId) {
        List<SysNoticeAttachment> nas = noticeAttachmentMapper.selectByNoticeId(noticeId);
        for (SysNoticeAttachment na : nas) {
            SysAttachment att = attachmentMapper.selectById(na.getFileId());
            if (att != null) {
                fileStorageService.delete(att.getPath());
                attachmentMapper.deleteById(att.getFileId());
            }
        }
    }

    private SysNotice requireNotice(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        return notice;
    }
}