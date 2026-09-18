package com.tianyu.oasystem.service.impl;

import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.FlowDefDTO;
import com.tianyu.oasystem.dto.FlowNodeDTO;
import com.tianyu.oasystem.entity.FlowDef;
import com.tianyu.oasystem.entity.FlowForm;
import com.tianyu.oasystem.entity.FlowNode;
import com.tianyu.oasystem.mapper.FlowDefMapper;
import com.tianyu.oasystem.mapper.FlowFormMapper;
import com.tianyu.oasystem.mapper.FlowInstanceMapper;
import com.tianyu.oasystem.mapper.FlowNodeMapper;
import com.tianyu.oasystem.service.FlowDefService;
import com.tianyu.oasystem.service.FlowFormAdminService;
import com.tianyu.oasystem.vo.FlowDefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowDefServiceImpl implements FlowDefService {

    private static final Set<String> APPROVER_TYPES =
            Set.of("ROLE", "DEPT_LEADER", "SPECIFIC_USER", "OWN_DEPT");

    private final FlowDefMapper defMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowFormMapper formMapper;
    private final FlowInstanceMapper instanceMapper;
    private final FlowFormAdminService formService;

    @Override
    public List<FlowDefVO> listDefs(boolean admin) {
        List<FlowDef> defs = admin ? defMapper.selectAll() : defMapper.selectEnabled();
        return defs.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    public FlowDefVO getDef(Long defId) {
        FlowDef def = requireDef(defId);
        return buildVO(def);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDef(FlowDefDTO dto) {
        checkForm(dto.getFormId());
        FlowDef def = new FlowDef();
        def.setFormId(dto.getFormId());
        def.setDefName(dto.getDefName());
        def.setCategory(dto.getCategory() == null ? "custom" : dto.getCategory());
        def.setBuiltIn(0);
        def.setStatus(1);
        defMapper.insert(def);
        saveNodesWithChain(def.getDefId(), dto.getNodes());
        return def.getDefId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDef(Long defId, FlowDefDTO dto) {
        FlowDef def = requireDef(defId);
        if (def.getBuiltIn() != null && def.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "内置流程不允许修改");
        }
        checkForm(dto.getFormId());
        def.setFormId(dto.getFormId());
        def.setDefName(dto.getDefName());
        def.setCategory(dto.getCategory() == null ? "custom" : dto.getCategory());
        defMapper.update(def);
        nodeMapper.deleteByDefId(defId);
        saveNodesWithChain(defId, dto.getNodes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDef(Long defId) {
        FlowDef def = requireDef(defId);
        if (def.getBuiltIn() != null && def.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "内置流程不允许删除");
        }
        if (instanceMapper.countByDefId(defId) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该流程已有审批单，不允许删除");
        }
        nodeMapper.deleteByDefId(defId);
        defMapper.deleteById(defId);
    }

    @Override
    public void toggleDef(Long defId) {
        FlowDef def = requireDef(defId);
        defMapper.updateStatus(defId, def.getStatus() == 1 ? 0 : 1);
    }

    /**
     * 插入节点并按节点列表下标的 nextIdx/branchToIdx 回填节点ID，
     * 从而生成串行链与分支链。
     */
    private void saveNodesWithChain(Long defId, List<FlowNodeDTO> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "流程至少需要一个审批节点");
        }
        List<FlowNode> saved = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            FlowNodeDTO dto = nodes.get(i);
            if (!"END".equals(dto.getNodeType()) && !APPROVER_TYPES.contains(dto.getApproverType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "节点审批人方式不合法");
            }
            FlowNode node = new FlowNode();
            node.setDefId(defId);
            node.setNodeName(dto.getNodeName());
            node.setNodeType(dto.getNodeType() == null ? "APPROVAL" : dto.getNodeType());
            node.setSortNo(i + 1);
            node.setApproverType(dto.getApproverType());
            node.setApproverIds(dto.getApproverIds() == null || dto.getApproverIds().isBlank() ? null : dto.getApproverIds());
            node.setBranchField(dto.getBranchField());
            node.setBranchOp(dto.getBranchOp());
            node.setBranchValue(dto.getBranchValue());
            nodeMapper.insert(node);
            saved.add(node);
        }
        for (int i = 0; i < nodes.size(); i++) {
            FlowNode node = saved.get(i);
            node.setBranchToNodeId(resolveIndex(nodes.get(i).getBranchToIdx(), saved));
            node.setNextNodeId(resolveIndex(nodes.get(i).getNextIdx(), saved));
            nodeMapper.update(node);
        }
    }

    private Long resolveIndex(int idx, List<FlowNode> saved) {
        return idx >= 0 && idx < saved.size() ? saved.get(idx).getNodeId() : null;
    }

    private FlowDefVO buildVO(FlowDef def) {
        FlowForm form = formMapper.selectById(def.getFormId());
        String formName = form == null ? "" : form.getFormName();
        List<FlowNode> nodes = nodeMapper.selectByDefId(def.getDefId());
        return FlowDefVO.from(def, formName, formService.loadFields(def.getFormId()), nodes);
    }

    private void checkForm(Long formId) {
        FlowForm form = formMapper.selectById(formId);
        if (form == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "所选表单不存在");
        }
    }

    private FlowDef requireDef(Long defId) {
        FlowDef def = defMapper.selectById(defId);
        if (def == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "流程不存在");
        }
        return def;
    }
}