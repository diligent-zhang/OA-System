package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.mapper.SysNoticeMapper;
import com.tianyu.oasystem.service.DashboardService;
import com.tianyu.oasystem.vo.DashboardStatsVO;
import com.tianyu.oasystem.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmpEmployeeMapper employeeMapper;
    private final SysDeptMapper deptMapper;
    private final SysNoticeMapper noticeMapper;

    @Override
    public DashboardStatsVO stats() {
        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setEmpTotal(employeeMapper.countAll());
        vo.setOnJobTotal(employeeMapper.countOnJob());
        vo.setDeptTotal(deptMapper.countAll());
        vo.setNoticeTotal(noticeMapper.countAll());
        return vo;
    }

    @Override
    public List<NameValueVO> deptEmpStats() {
        return employeeMapper.selectDeptEmpStats();
    }
}
