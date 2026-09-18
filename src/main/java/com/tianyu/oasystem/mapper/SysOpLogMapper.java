package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.SysOpLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysOpLogMapper {

    int insert(SysOpLog log);

    /** 分页查询：user（本人/某用户）、opType、时间区间 */
    List<SysOpLog> selectPage(@Param("userId") Long userId,
                              @Param("opType") String opType,
                              @Param("beginTime") String beginTime,
                              @Param("endTime") String endTime);
}