package com.tianyu.oasystem.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 通讯录部门树节点 */
@Data
public class ContactsNodeVO {

    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer sortNo;
    private List<ContactsNodeVO> children = new ArrayList<>();
    private List<ContactVO> employees = new ArrayList<>();
}