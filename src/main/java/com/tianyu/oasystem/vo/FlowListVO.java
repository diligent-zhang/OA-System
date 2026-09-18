package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 审批单列表项（我发起的 / 归档） */
@Data
public class FlowListVO {

    private Long instanceId;
    private Long defId;
    private String defName;
    private String category;
    private String title;
    private String applicantName;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
}