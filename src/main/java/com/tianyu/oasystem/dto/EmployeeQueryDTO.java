package com.tianyu.oasystem.dto;

import lombok.Data;

/** 员工分页多条件查询参数 */
@Data
public class EmployeeQueryDTO {

    private String name;
    private String empNo;
    private Long deptId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
