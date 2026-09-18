package com.tianyu.oasystem.controller;

import com.tianyu.oasystem.common.Result;
import com.tianyu.oasystem.service.ContactsService;
import com.tianyu.oasystem.vo.ContactVO;
import com.tianyu.oasystem.vo.ContactsNodeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 企业通讯录（登录即可，只读） */
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactsController {

    private final ContactsService contactsService;

    @GetMapping("/tree")
    public Result<List<ContactsNodeVO>> tree() {
        return Result.success(contactsService.tree());
    }

    @GetMapping("/query")
    public Result<List<ContactVO>> query(@RequestParam(required = false) String keyword) {
        return Result.success(contactsService.query(keyword));
    }
}