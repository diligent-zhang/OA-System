package com.tianyu.oasystem.service;

import com.tianyu.oasystem.entity.FlowInstance;

import java.util.Map;

/**
 * 审批通过联动考勤的消费者。
 * 由考勤模块注册实现；流程引擎审批通过（请假/加班类别）时回调。
 */
public interface FlowAttendanceNotifier {

    void onApproved(FlowInstance instance, Map<String, Object> formData);
}