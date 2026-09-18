package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.annotation.OpLog;
import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.dto.PunchDTO;
import com.tianyu.oasystem.entity.AttRecord;
import com.tianyu.oasystem.entity.AttRule;
import com.tianyu.oasystem.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 员工打卡与个人考勤 */
@RestController
@RequestMapping("/api/att")
@RequiredArgsConstructor
public class AttController {

    private final AttendanceService attendanceService;

    @GetMapping("/rule/active")
    public Result<AttRule> activeRule() {
        return Result.success(attendanceService.activeRule());
    }

    @PostMapping("/punch")
    @OpLog(type = "ATT", biz = "考勤", op = "打卡")
    public Result<String> punch(@Valid @RequestBody PunchDTO dto) {
        return Result.success(attendanceService.punch(dto), null);
    }

    @GetMapping("/my-records")
    public Result<List<AttRecord>> myRecords(@RequestParam(required = false) String month) {
        return Result.success(attendanceService.myMonthly(month));
    }
}