package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttRule {

    private Long ruleId;
    private String ruleName;
    private String workStart;
    private String workEnd;
    private Double latitude;
    private Double longitude;
    private Integer radiusM;
    private Integer locationCheck;
    private String restDays;
    private Integer enabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}