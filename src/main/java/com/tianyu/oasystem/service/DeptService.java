package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.dto.DeptDTO;
import com.tianyu.oasystem.vo.DeptVO;

import java.util.List;

public interface DeptService {

    PageResult<DeptVO> page(String keyword, Integer pageNum, Integer pageSize);

    /** 两级树形结构 */
    List<DeptVO> tree();

    void add(DeptDTO dto);

    void update(Long deptId, DeptDTO dto);

    /** 存在在职员工或子部门时禁止删除 */
    void delete(Long deptId);
}
