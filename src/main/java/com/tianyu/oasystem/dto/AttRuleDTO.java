package com.tianyu.oasystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 考勤规则配置 */
@Data
public class AttRuleDTO {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "上班时间不能为空")
    private String workStart;

    @NotBlank(message = "下班时间不能为空")
    private String workEnd;

    private Double latitude;
    private Double longitude;
    private Integer radiusM = 200;
    private Integer locationCheck = 0;

    /** 休息日：0~6 表示 周日~周六 */
    private List<String> restDays;
}