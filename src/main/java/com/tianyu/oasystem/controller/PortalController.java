package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.service.FlowInstanceService;
import com.tianyu.oasystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 工作台门户聚合 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final FlowInstanceService flowInstanceService;
    private final MessageService messageService;

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("todoCount", flowInstanceService.countTodo());
        summary.put("processingCount", flowInstanceService.countProcessing());
        summary.put("unreadMessageCount", messageService.unreadCount());
        return Result.success(summary);
    }
}