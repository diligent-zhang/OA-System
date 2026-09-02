package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.dto.NoticeDTO;
import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.vo.NoticeVO;

import java.util.List;

public interface NoticeService {

    PageResult<NoticeVO> adminPage(NoticeQueryDTO query);

    PageResult<NoticeVO> publishedPage(NoticeQueryDTO query);

    NoticeVO detail(Long noticeId);

    void add(NoticeDTO dto);

    void update(Long noticeId, NoticeDTO dto);

    void delete(Long noticeId);

    /** 上架/下架切换 */
    void toggleStatus(Long noticeId);

    List<NoticeVO> latest(int limit);
}
