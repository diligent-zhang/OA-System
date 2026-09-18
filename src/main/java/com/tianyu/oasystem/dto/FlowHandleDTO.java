package com.tianyu.oasystem.dto;

import lombok.Data;

/** 审批处理参数：同意/驳回/转办/补充意见 */
@Data
public class FlowHandleDTO {

    private String comment;

    /** 转办目标用户ID */
    private Long toUserId;

    /** 补充意见内容 */
    private String content;
}