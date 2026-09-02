package com.tianyu.oasystem.service.impl;

import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.DeptDTO;
import com.tianyu.oasystem.entity.SysDept;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.SysDeptMapper;
import com.tianyu.oasystem.service.DeptService;
import com.tianyu.oasystem.vo.DeptVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final SysDeptMapper deptMapper;
    private final EmpEmployeeMapper employeeMapper;

    @Override
    public PageResult<DeptVO> page(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(deptMapper.selectPage(keyword));
    }

    @Override
    public List<DeptVO> tree() {
        List<DeptVO> all = deptMapper.selectAllVO();
        List<DeptVO> roots = new ArrayList<>();
        for (DeptVO dept : all) {
            if (dept.getParentId() == null || dept.getParentId() == 0L) {
                roots.add(dept);
            }
        }
        for (DeptVO root : roots) {
            List<DeptVO> children = new ArrayList<>();
            for (DeptVO dept : all) {
                if (root.getDeptId().equals(dept.getParentId())) {
                    children.add(dept);
                }
            }
            root.setChildren(children);
        }
        return roots;
    }

    @Override
    public void add(DeptDTO dto) {
        checkParentLevel(dto.getParentId(), null);
        checkDuplicateName(dto, null);
        SysDept dept = new SysDept();
        dept.setDeptName(dto.getDeptName());
        dept.setParentId(dto.getParentId());
        dept.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        deptMapper.insert(dept);
    }

    @Override
    public void update(Long deptId, DeptDTO dto) {
        SysDept exist = deptMapper.selectById(deptId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        if (deptId.equals(dto.getParentId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上级部门不能是自己");
        }
        // 一级部门下挂有子部门时不能再降级为二级，避免出现三级结构
        if (dto.getParentId() != 0L && deptMapper.countChildren(deptId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该部门下存在子部门，不能变更为非一级部门");
        }
        checkParentLevel(dto.getParentId(), deptId);
        checkDuplicateName(dto, deptId);
        SysDept dept = new SysDept();
        dept.setDeptId(deptId);
        dept.setDeptName(dto.getDeptName());
        dept.setParentId(dto.getParentId());
        dept.setSortNo(dto.getSortNo() == null ? 0 : dto.getSortNo());
        deptMapper.update(dept);
    }

    @Override
    public void delete(Long deptId) {
        SysDept exist = deptMapper.selectById(deptId);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        if (deptMapper.countChildren(deptId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该部门下存在子部门，无法删除");
        }
        int empCount = employeeMapper.countByDeptId(deptId);
        if (empCount > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该部门下存在在职员工，无法删除");
        }
        deptMapper.deleteById(deptId);
    }

    /** 仅允许两级：父部门必须是一级部门（或 0） */
    private void checkParentLevel(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上级部门不存在");
        }
        if (parent.getParentId() != 0L) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最多支持两级部门");
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "上级部门不能是自己");
        }
    }

    private void checkDuplicateName(DeptDTO dto, Long excludeId) {
        if (deptMapper.countByNameAndParent(dto.getDeptName(), dto.getParentId(), excludeId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "同级部门下已存在同名部门");
        }
    }
}
