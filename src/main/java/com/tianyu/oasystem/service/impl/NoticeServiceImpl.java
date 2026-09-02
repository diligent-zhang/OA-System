package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.NoticeDTO;
import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.entity.SysNotice;
import com.tianyu.oasystem.mapper.SysNoticeMapper;
import com.tianyu.oasystem.service.NoticeService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final SysNoticeMapper noticeMapper;

    @Override
    public PageResult<NoticeVO> adminPage(NoticeQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return PageResult.of(noticeMapper.selectPageAdmin(query));
    }

    @Override
    public PageResult<NoticeVO> publishedPage(NoticeQueryDTO query) {
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return PageResult.of(noticeMapper.selectPagePublished(query));
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
        return vo;
    }

    @Override
    public void add(NoticeDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setTopFlag(dto.getTopFlag() == null ? 0 : dto.getTopFlag());
        notice.setStatus(Boolean.FALSE.equals(dto.getPublish()) ? 0 : 1);
        notice.setPublisherId(UserContext.userId());
        noticeMapper.insert(notice);
    }

    @Override
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
    }

    @Override
    public void delete(Long noticeId) {
        requireNotice(noticeId);
        noticeMapper.deleteById(noticeId);
    }

    @Override
    public void toggleStatus(Long noticeId) {
        SysNotice exist = requireNotice(noticeId);
        noticeMapper.updateStatus(noticeId, exist.getStatus() == 1 ? 0 : 1);
    }

    @Override
    public List<NoticeVO> latest(int limit) {
        return noticeMapper.selectLatest(limit);
    }

    private SysNotice requireNotice(Long noticeId) {
        SysNotice notice = noticeMapper.selectById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }
        return notice;
    }
}
