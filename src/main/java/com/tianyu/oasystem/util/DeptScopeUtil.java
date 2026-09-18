package com.tianyu.oasystem.util;

import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.vo.DeptVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 部门数据范围：ADMIN 不限；DEPT_ADMIN/EMPLOYEE 限定本部门及直属子部门 */
@Component
@RequiredArgsConstructor
public class DeptScopeUtil {

    private final SysDeptMapper deptMapper;

    /** 当前登录用户可管理的部门ID集合；ADMIN 返回 null 表示不限 */
    public List<Long> scopedDeptIds() {
        UserPrincipal principal = UserContext.get();
        if (principal == null || principal.isAdmin() || principal.getDeptId() == null) {
            return null;
        }
        List<DeptVO> all = deptMapper.selectAllVO();
        List<Long> result = new ArrayList<>();
        result.add(principal.getDeptId());
        collectChildren(principal.getDeptId(), all, result);
        return result;
    }

    private void collectChildren(Long parentId, List<DeptVO> all, List<Long> result) {
        for (DeptVO d : all) {
            if (Objects.equals(d.getParentId(), parentId)) {
                result.add(d.getDeptId());
                collectChildren(d.getDeptId(), all, result);
            }
        }
    }
}