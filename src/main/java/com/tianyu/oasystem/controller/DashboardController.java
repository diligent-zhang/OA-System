package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.service.DashboardService;
import com.tianyu.oasystem.service.NoticeService;
import com.tianyu.oasystem.vo.DashboardStatsVO;
import com.tianyu.oasystem.vo.NameValueVO;
import com.tianyu.oasystem.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 系统仪表盘（管理员端） */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final NoticeService noticeService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.stats());
    }

    @GetMapping("/dept-emp")
    public Result<List<NameValueVO>> deptEmpStats() {
        return Result.success(dashboardService.deptEmpStats());
    }

    @GetMapping("/latest-notices")
    public Result<List<NoticeVO>> latestNotices() {
        return Result.success(noticeService.latest(5));
    }
}
