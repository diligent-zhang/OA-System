package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.NoticeDTO;
import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.service.NoticeService;
import com.tianyu.oasystem.vo.NoticeReadStatsVO;
import com.tianyu.oasystem.vo.NoticeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公告：管理端 /api/admin/notices，员工端 /api/notices（v2：附件、已读统计） */
@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ==================== 管理员端 ====================

    @GetMapping("/api/admin/notices")
    public Result<PageResult<NoticeVO>> adminPage(NoticeQueryDTO query) {
        return Result.success(noticeService.adminPage(query));
    }

    @PostMapping("/api/admin/notices")
    @OpLog(type = "NOTICE", biz = "公告", op = "发布")
    public Result<Void> add(@Valid @RequestBody NoticeDTO dto) {
        noticeService.add(dto);
        return Result.success("公告发布成功", null);
    }

    @PutMapping("/api/admin/notices/{id}")
    @OpLog(type = "NOTICE", biz = "公告", op = "编辑")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody NoticeDTO dto) {
        noticeService.update(id, dto);
        return Result.success("公告已更新", null);
    }

    @DeleteMapping("/api/admin/notices/{id}")
    @OpLog(type = "NOTICE", biz = "公告", op = "删除")
    public Result<Void> delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return Result.success("公告已删除", null);
    }

    @PutMapping("/api/admin/notices/{id}/toggle")
    @OpLog(type = "NOTICE", biz = "公告", op = "上下架切换")
    public Result<Void> toggle(@PathVariable("id") Long id) {
        noticeService.toggleStatus(id);
        return Result.success("上下架状态已切换", null);
    }

    @DeleteMapping("/api/admin/notices/{noticeId}/attachments/{attachmentId}")
    @OpLog(type = "FILE", biz = "公告附件", op = "删除")
    public Result<Void> deleteAttachment(@PathVariable("noticeId") Long noticeId,
                                         @PathVariable("attachmentId") Long attachmentId) {
        noticeService.deleteAttachment(noticeId, attachmentId);
        return Result.success("附件已删除", null);
    }

    @GetMapping("/api/admin/notices/{id}/read-stats")
    public Result<NoticeReadStatsVO> readStats(@PathVariable("id") Long id) {
        return Result.success(noticeService.readStats(id));
    }

    // ==================== 员工端 ====================

    @GetMapping("/api/notices")
    public Result<PageResult<NoticeVO>> publishedPage(NoticeQueryDTO query) {
        return Result.success(noticeService.publishedPage(query));
    }

    @GetMapping("/api/notices/{id}")
    public Result<NoticeVO> detail(@PathVariable("id") Long id) {
        return Result.success(noticeService.detail(id));
    }
}