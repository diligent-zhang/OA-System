package com.tianyu.oasystem.service;

import com.tianyu.oasystem.vo.ContactVO;
import com.tianyu.oasystem.vo.ContactsNodeVO;

import java.util.List;

/** 企业通讯录 */
public interface ContactsService {

    /** 按部门树返回通讯录（含部门下员工） */
    List<ContactsNodeVO> tree();

    /** 按姓名/工号/岗位/手机号/部门检索 */
    List<ContactVO> query(String keyword);
}