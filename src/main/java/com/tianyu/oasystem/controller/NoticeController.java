package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.NoticeDTO;
import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.service.NoticeService;
import com.tianyu.oasystem.vo.NoticeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 公告：管理端 /api/admin/notices，员工端 /api/notices */
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
    public Result<Void> add(@Valid @RequestBody NoticeDTO dto) {
        noticeService.add(dto);
        return Result.success("公告发布成功", null);
    }

    @PutMapping("/api/admin/notices/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody NoticeDTO dto) {
        noticeService.update(id, dto);
        return Result.success("公告已更新", null);
    }

    @DeleteMapping("/api/admin/notices/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return Result.success("公告已删除", null);
    }

    @PutMapping("/api/admin/notices/{id}/toggle")
    public Result<Void> toggle(@PathVariable("id") Long id) {
        noticeService.toggleStatus(id);
        return Result.success("上下架状态已切换", null);
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
