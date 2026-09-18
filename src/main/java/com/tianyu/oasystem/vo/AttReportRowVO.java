package com.tianyu.oasystem.vo;

import lombok.Data;

/** 月度考勤报表行 */
@Data
public class AttReportRowVO {

    private Long empId;
    private String empNo;
    private String name;
    private String deptName;
    private Integer shouldDays = 0;
    private Integer actualDays = 0;
    private Integer lateCount = 0;
    private Integer earlyCount = 0;
    private Integer missingCount = 0;
    private Integer leaveDays = 0;
    private Integer overtimeCount = 0;
    private Double overtimeHours = 0.0;
    private Integer absentDays = 0;
}