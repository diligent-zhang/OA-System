package com.tianyu.oasystem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 部门视图：分页列表 + 树形 children */
@Data
public class DeptVO {

    private Long deptId;
    private String deptName;
    private Long parentId;
    private String parentName;
    private Integer sortNo;
    /** 部门下员工人数（含子部门） */
    private Integer empCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private List<DeptVO> children;
}
