package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.entity.FlowInstance;
import com.tianyu.oasystem.vo.FlowListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FlowInstanceMapper {

    FlowInstance selectById(@Param("instanceId") Long instanceId);

    int insert(FlowInstance instance);

    int updateStatus(@Param("instanceId") Long instanceId, @Param("status") String status,
                     @Param("finishTime") String finishTime);

    int updateCurrentNode(@Param("instanceId") Long instanceId, @Param("nodeId") Long nodeId);

    int markDeleted(@Param("instanceId") Long instanceId);

    /** 我发起的 */
    List<FlowListVO> selectPageByApplicant(@Param("applicantUserId") Long userId,
                                           @Param("status") String status,
                                           @Param("keyword") String keyword);

    /** 管理归档：全部（含指定状态/关键词筛选） */
    List<FlowListVO> selectArchivePage(@Param("status") String status,
                                       @Param("keyword") String keyword,
                                       @Param("category") String category);

    /** 审批中实例数（发起人） */
    int countProcessingByApplicant(@Param("applicantUserId") Long userId);

    /** 某流程下的实例数（删除保护） */
    int countByDefId(@Param("defId") Long defId);
}