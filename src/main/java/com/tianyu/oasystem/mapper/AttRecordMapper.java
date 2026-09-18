package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.AttRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttRecordMapper {

    AttRecord selectByEmpAndDate(@Param("empId") Long empId, @Param("date") LocalDate date);

    int insert(AttRecord record);

    int updateClockIn(@Param("id") Long id, @Param("clockIn") java.time.LocalDateTime clockIn,
                      @Param("status") String status);

    int updateClockOut(@Param("id") Long id, @Param("clockOut") java.time.LocalDateTime clockOut,
                       @Param("status") String status);

    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("remark") String remark);

    /** 个人按月明细 */
    List<AttRecord> selectByEmpMonth(@Param("empId") Long empId,
                                     @Param("begin") LocalDate begin, @Param("end") LocalDate end);

    /** 全员按月明细（含部门，报表聚合用，可带部门范围） */
    List<AttRecord> selectMonth(@Param("begin") LocalDate begin, @Param("end") LocalDate end,
                                @Param("deptIds") List<Long> deptIds);
}