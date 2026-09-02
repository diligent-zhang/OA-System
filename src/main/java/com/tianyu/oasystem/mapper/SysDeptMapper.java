package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysDept;
import com.tianyu.oasystem.vo.DeptVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper {

    /** 分页 + 关键词查询（含父部门名与员工数） */
    List<DeptVO> selectPage(@Param("keyword") String keyword);

    List<DeptVO> selectAllVO();

    SysDept selectById(@Param("deptId") Long deptId);

    int countByNameAndParent(@Param("deptName") String deptName,
                             @Param("parentId") Long parentId,
                             @Param("excludeId") Long excludeId);

    int countChildren(@Param("parentId") Long parentId);

    int countAll();

    int insert(SysDept dept);

    int update(SysDept dept);

    int deleteById(@Param("deptId") Long deptId);
}
