package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlowInstance {

    private Long instanceId;
    private Long defId;
    private Long formId;
    private String defName;
    private String category;
    private String title;
    private Long applicantUserId;
    private Long applicantEmpId;
    private String applicantName;
    private String formDataJson;
    private String status;
    private Long currentNodeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    private Integer deleted;
}