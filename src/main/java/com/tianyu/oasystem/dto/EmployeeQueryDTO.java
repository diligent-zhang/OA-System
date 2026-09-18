package com.tianyu.oasystem.dto;

import lombok.Data;

import java.util.List;

/** 员工分页多条件查询参数 */
@Data
public class EmployeeQueryDTO {

    private String name;
    private String empNo;
    private Long deptId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 部门数据范围（部门管理员限定，管理员为空表示不限） */
    private List<Long> deptIds;
}
