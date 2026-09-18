package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowTask;
import com.tianyu.oasystem.vo.FlowTodoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowTaskMapper {

    FlowTask selectById(@Param("taskId") Long taskId);

    int insert(FlowTask task);

    List<FlowTask> selectByInstance(@Param("instanceId") Long instanceId);

    /** 待我审批 */
    List<FlowTodoVO> selectTodoPage(@Param("userId") Long userId);

    /** 我处理过的（含同意/驳回/转办） */
    List<FlowTodoVO> selectDonePage(@Param("userId") Long userId);

    /** 实例下待处理的 PENDING 任务 */
    List<FlowTask> selectPendingByInstance(@Param("instanceId") Long instanceId);

    int updateHandle(@Param("taskId") Long taskId, @Param("status") String status,
                     @Param("comment") String comment, @Param("handleTime") String handleTime);

    /** 转办：原任务标记 TRANSFERRED */
    int markTransfer(@Param("taskId") Long taskId, @Param("comment") String comment,
                     @Param("handleTime") String handleTime);

    /** 实例下未完成任务数 */
    int countPendingByInstance(@Param("instanceId") Long instanceId);

    /** 我的待办任务数（门户角标） */
    int countTodoByUser(@Param("userId") Long userId);
}