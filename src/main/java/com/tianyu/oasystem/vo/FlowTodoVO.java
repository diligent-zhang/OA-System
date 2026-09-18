package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 待我审批 / 我审批过的列表项 */
@Data
public class FlowTodoVO {

    private Long taskId;
    private Long instanceId;
    private String defName;
    private String category;
    private String title;
    private String applicantName;
    private String nodeName;
    private String taskStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
}