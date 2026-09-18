package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.service.ContactsService;
import com.tianyu.oasystem.vo.ContactVO;
import com.tianyu.oasystem.vo.ContactsNodeVO;
import com.tianyu.oasystem.vo.DeptVO;
import com.tianyu.oasystem.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactsServiceImpl implements ContactsService {

    private final SysDeptMapper deptMapper;
    private final EmpEmployeeMapper employeeMapper;

    @Override
    public List<ContactsNodeVO> tree() {
        List<DeptVO> depts = deptMapper.selectAllVO();
        List<EmployeeVO> emps = employeeMapper.selectActiveList(null);
        Map<Long, List<ContactVO>> byDept = emps.stream()
                .map(this::toContact)
                .filter(c -> c.getDeptId() != null)
                .collect(Collectors.groupingBy(ContactVO::getDeptId));

        List<ContactsNodeVO> nodes = depts.stream().map(d -> {
            ContactsNodeVO node = new ContactsNodeVO();
            node.setDeptId(d.getDeptId());
            node.setDeptName(d.getDeptName());
            node.setParentId(d.getParentId());
            node.setSortNo(d.getSortNo());
            node.setEmployees(byDept.getOrDefault(d.getDeptId(), new ArrayList<>()));
            return node;
        }).collect(Collectors.toList());

        List<ContactsNodeVO> roots = new ArrayList<>();
        for (ContactsNodeVO node : nodes) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                nodes.stream()
                        .filter(p -> Objects.equals(p.getDeptId(), node.getParentId()))
                        .findFirst()
                        .ifPresent(p -> p.getChildren().add(node));
            }
        }
        return roots;
    }

    @Override
    public List<ContactVO> query(String keyword) {
        List<EmployeeVO> emps = employeeMapper.selectActiveList(null);
        List<ContactVO> result = emps.stream().map(this::toContact).collect(Collectors.toList());
        if (!StringUtils.hasText(keyword)) {
            return result;
        }
        String kw = keyword.trim().toLowerCase();
        return result.stream()
                .filter(c -> contains(c.getName(), kw) || contains(c.getEmpNo(), kw)
                        || contains(c.getPosition(), kw) || contains(c.getPhone(), kw)
                        || contains(c.getDeptName(), kw))
                .collect(Collectors.toList());
    }

    private boolean contains(String value, String kw) {
        return value != null && value.toLowerCase().contains(kw);
    }

    private ContactVO toContact(EmployeeVO e) {
        ContactVO c = new ContactVO();
        c.setEmpId(e.getEmpId());
        c.setUserId(e.getUserId());
        c.setEmpNo(e.getEmpNo());
        c.setName(e.getName());
        c.setGender(e.getGender());
        c.setPosition(e.getPosition());
        c.setPhone(e.getPhone());
        c.setEmail(e.getEmail());
        c.setDeptId(e.getDeptId());
        c.setDeptName(e.getDeptName());
        return c;
    }
}