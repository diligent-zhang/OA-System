package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tianyu.oasystem.entity.FlowComment;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.entity.FlowTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 审批单详情（含表单字段/数据/链路/附件） */
@Data
public class FlowDetailVO {

    private Long instanceId;
    private Long defId;
    private Long formId;
    private String defName;
    private String category;
    private String title;
    private Long applicantUserId;
    private String applicantName;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /** 当前待办任务ID（我待处理时返回，便于操作） */
    private Long myTodoTaskId;

    /** 表单字段定义 */
    private List<FlowFormField> fields;

    /** 表单数据 */
    private Map<String, Object> formData;

    /** 审批链路时间线 */
    private List<FlowComment> comments;

    /** 任务明细（节点状态） */
    private List<FlowTask> tasks;

    /** 附件（原始文件信息） */
    private List<AttachmentVO> attachments;
}