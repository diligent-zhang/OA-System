package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.FlowDefDTO;
import com.tianyu.oasystem.vo.FlowDefVO;

import java.util.List;

/** 流程定义（绑定表单 + 审批节点链） */
public interface FlowDefService {

    /** 管理员：全部；否则仅启用 */
    List<FlowDefVO> listDefs(boolean admin);

    FlowDefVO getDef(Long defId);

    Long createDef(FlowDefDTO dto);

    void updateDef(Long defId, FlowDefDTO dto);

    void deleteDef(Long defId);

    void toggleDef(Long defId);
}