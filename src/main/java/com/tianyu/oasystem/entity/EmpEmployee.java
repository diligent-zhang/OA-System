package com.tianyu.oasystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmpEmployee {

    private Long empId;
    private String empNo;
    private String name;
    private Integer gender;
    private String phone;
    private String email;
    private Long deptId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
