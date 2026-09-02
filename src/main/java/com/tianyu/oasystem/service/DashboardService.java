package com.tianyu.oasystem.service;

import com.tianyu.oasystem.vo.DashboardStatsVO;
import com.tianyu.oasystem.vo.NameValueVO;

import java.util.List;

public interface DashboardService {

    DashboardStatsVO stats();

    /** 各部门在职人数占比（饼图/柱图数据） */
    List<NameValueVO> deptEmpStats();
}
