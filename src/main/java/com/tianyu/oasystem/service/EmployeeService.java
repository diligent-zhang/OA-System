package com.tianyu.oasystem.service;

import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.dto.EmployeeDTO;
import com.tianyu.oasystem.dto.EmployeeQueryDTO;
import com.tianyu.oasystem.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService {

    PageResult<EmployeeVO> page(EmployeeQueryDTO query);

    EmployeeVO getById(Long empId);

    void add(EmployeeDTO dto);

    void update(Long empId, EmployeeDTO dto);

    void delete(Long empId);

    void deleteBatch(List<Long> empIds);

    /** 重置登录密码为 123456 */
    void resetPassword(Long empId);

    /** 禁用/启用登录账号 */
    void updateAccountStatus(Long empId, Integer status);
}
