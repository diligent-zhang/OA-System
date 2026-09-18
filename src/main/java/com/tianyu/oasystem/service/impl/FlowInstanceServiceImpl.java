package com.tianyu.oasystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.tianyu.oasystem.common.BusinessException;
import com.tianyu.oasystem.common.PageResult;
import com.tianyu.oasystem.common.ResultCode;
import com.tianyu.oasystem.dto.FlowHandleDTO;
import com.tianyu.oasystem.dto.FlowSubmitDTO;
import com.tianyu.oasystem.entity.EmpEmployee;
import com.tianyu.oasystem.entity.FlowComment;
import com.tianyu.oasystem.entity.FlowDef;
import com.tianyu.oasystem.entity.FlowForm;
import com.tianyu.oasystem.entity.FlowFormField;
import com.tianyu.oasystem.entity.FlowInstance;
import com.tianyu.oasystem.entity.FlowNode;
import com.tianyu.oasystem.entity.FlowTask;
import com.tianyu.oasystem.entity.SysAttachment;
import com.tianyu.oasystem.entity.SysUser;
import com.tianyu.oasystem.mapper.EmpEmployeeMapper;
import com.tianyu.oasystem.mapper.FlowCommentMapper;
import com.tianyu.oasystem.mapper.FlowDefMapper;
import com.tianyu.oasystem.mapper.FlowFormMapper;
import com.tianyu.oasystem.mapper.FlowInstanceMapper;
import com.tianyu.oasystem.mapper.FlowNodeMapper;
import com.tianyu.oasystem.mapper.FlowTaskMapper;
import com.tianyu.oasystem.mapper.SysAttachmentMapper;
import com.tianyu.oasystem.mapper.SysUserMapper;
import com.tianyu.oasystem.service.FlowAttendanceNotifier;
import com.tianyu.oasystem.service.FlowFormAdminService;
import com.tianyu.oasystem.service.FlowInstanceService;
import com.tianyu.oasystem.service.MessageService;
import com.tianyu.oasystem.util.UserContext;
import com.tianyu.oasystem.util.UserPrincipal;
import com.tianyu.oasystem.vo.AttachmentVO;
import com.tianyu.oasystem.vo.FlowDetailVO;
import com.tianyu.oasystem.vo.FlowListVO;
import com.tianyu.oasystem.vo.FlowTodoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowInstanceServiceImpl implements FlowInstanceService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ST_APPROVING = "APPROVING";
    private static final String ST_APPROVED = "APPROVED";
    private static final String ST_REJECTED = "REJECTED";
    private static final String ST_WITHDRAWN = "WITHDRAWN";

    private final FlowDefMapper defMapper;
    private final FlowFormMapper formMapper;
    private final FlowNodeMapper nodeMapper;
    private final FlowInstanceMapper instanceMapper;
    private final FlowTaskMapper taskMapper;
    private final FlowCommentMapper commentMapper;
    private final SysUserMapper userMapper;
    private final EmpEmployeeMapper employeeMapper;
    private final SysAttachmentMapper attachmentMapper;
    private final FlowFormAdminService formService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final List<FlowAttendanceNotifier> attendanceNotifiers;

    // ==================== 发起 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(FlowSubmitDTO dto) {
        FlowDef def = defMapper.selectById(dto.getDefId());
        if (def == null || def.getStatus() == null || def.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "流程不存在或已停用");
        }
        List<FlowFormField> fields = formService.loadFields(def.getFormId());
        Map<String, Object> data = dto.getFormData() == null ? new HashMap<>() : dto.getFormData();
        validateFields(fields, data);

        UserPrincipal me = UserContext.get();
        FlowInstance inst = new FlowInstance();
        inst.setDefId(def.getDefId());
        inst.setFormId(def.getFormId());
        inst.setDefName(def.getDefName());
        inst.setCategory(def.getCategory());
        inst.setTitle(dto.getTitle());
        inst.setApplicantUserId(me.getUserId());
        inst.setApplicantEmpId(me.getEmpId());
        inst.setApplicantName(loadUserName(me.getUserId()));
        inst.setStatus(ST_APPROVING);
        inst.setDeleted(0);
        inst.setFormDataJson(writeJson(data));

        List<FlowNode> nodes = nodeMapper.selectByDefId(def.getDefId());
        FlowNode first = nodes.stream()
                .filter(n -> !"END".equals(n.getNodeType()))
                .min(Comparator.comparingInt(n -> n.getSortNo() == null ? 0 : n.getSortNo()))
                .orElse(null);
        if (first == null) {
            // 无审批节点的流程：提交即自动通过
            instanceMapper.insert(inst);
            addComment(inst.getInstanceId(), null, "SUBMIT", dto.getTitle());
            finishApproved(inst, data);
            return inst.getInstanceId();
        }
        inst.setCurrentNodeId(first.getNodeId());
        instanceMapper.insert(inst);
        rebindAttachments(fields, data, inst.getInstanceId());
        addComment(inst.getInstanceId(), null, "SUBMIT", dto.getTitle());
        createTask(inst, first, data);
        return inst.getInstanceId();
    }

    // ==================== 查询 ====================

    @Override
    public PageResult<FlowListVO> myPage(String status, String keyword, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(instanceMapper.selectPageByApplicant(UserContext.userId(), status, keyword));
    }

    @Override
    public PageResult<FlowTodoVO> todoPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(taskMapper.selectTodoPage(UserContext.userId()));
    }

    @Override
    public PageResult<FlowTodoVO> donePage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(taskMapper.selectDonePage(UserContext.userId()));
    }

    @Override
    public PageResult<FlowListVO> archivePage(String status, String category, String keyword,
                                              int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(instanceMapper.selectArchivePage(status, keyword, category));
    }

    @Override
    public FlowDetailVO detail(Long instanceId) {
        FlowInstance inst = requireInstance(instanceId);
        checkReadAccess(inst);
        FlowDetailVO vo = new FlowDetailVO();
        vo.setInstanceId(inst.getInstanceId());
        vo.setDefId(inst.getDefId());
        vo.setFormId(inst.getFormId());
        vo.setDefName(inst.getDefName());
        vo.setCategory(inst.getCategory());
        vo.setTitle(inst.getTitle());
        vo.setApplicantUserId(inst.getApplicantUserId());
        vo.setApplicantName(inst.getApplicantName());
        vo.setStatus(inst.getStatus());
        vo.setCreateTime(inst.getCreateTime());
        vo.setFinishTime(inst.getFinishTime());
        vo.setFields(formService.loadFields(inst.getFormId()));
        vo.setFormData(readJson(inst.getFormDataJson()));
        vo.setComments(commentMapper.selectByInstance(instanceId));
        vo.setTasks(taskMapper.selectByInstance(instanceId));
        vo.setAttachments(loadAttachments(instanceId));

        List<FlowTask> mine = taskMapper.selectByInstance(instanceId).stream()
                .filter(t -> "PENDING".equals(t.getStatus()) && t.getAssigneeUserId().equals(UserContext.userId()))
                .findFirst()
                .map(List::of)
                .orElse(new ArrayList<>());
        if (!mine.isEmpty()) {
            vo.setMyTodoTaskId(mine.get(0).getTaskId());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdraw(Long instanceId) {
        FlowInstance inst = requireInstance(instanceId);
        if (!inst.getApplicantUserId().equals(UserContext.userId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅发起人可撤回");
        }
        if (!ST_APPROVING.equals(inst.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "仅审批中的单据可撤回");
        }
        instanceMapper.updateStatus(instanceId, ST_WITHDRAWN, now());
        taskMapper.selectPendingByInstance(instanceId)
                .forEach(t -> taskMapper.updateHandle(t.getTaskId(), "WITHDRAWN", "发起人已撤回", now()));
        addComment(instanceId, null, "WITHDRAW", "发起人撤回该审批");
    }

    // ==================== 任务处理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long taskId, FlowHandleDTO dto) {
        FlowTask task = requireMinePending(taskId);
        FlowInstance inst = requireInstance(task.getInstanceId());
        if (!ST_APPROVING.equals(inst.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审批已结束，无法处理");
        }
        String comment = dto == null ? null : dto.getComment();
        taskMapper.updateHandle(taskId, "APPROVED", comment, now());
        addComment(inst.getInstanceId(), taskId, "APPROVE", comment);
        advanceAfterApprove(inst, task, comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long taskId, FlowHandleDTO dto) {
        FlowTask task = requireMinePending(taskId);
        FlowInstance inst = requireInstance(task.getInstanceId());
        if (!ST_APPROVING.equals(inst.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审批已结束，无法处理");
        }
        String comment = dto == null ? null : dto.getComment();
        taskMapper.updateHandle(taskId, "REJECTED", comment, now());
        instanceMapper.updateStatus(inst.getInstanceId(), ST_REJECTED, now());
        addComment(inst.getInstanceId(), taskId, "REJECT", comment);
        messageService.send(inst.getApplicantUserId(), 1,
                "审批被驳回：" + inst.getDefName(),
                "《" + inst.getTitle() + "》在【" + task.getNodeName() + "】被驳回" +
                        (StringUtils.hasText(comment) ? "：" + comment : ""),
                "/flow/detail/" + inst.getInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Long taskId, FlowHandleDTO dto) {
        FlowTask task = requireMinePending(taskId);
        FlowInstance inst = requireInstance(task.getInstanceId());
        if (!ST_APPROVING.equals(inst.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "审批已结束，无法转办");
        }
        Long toUserId = dto == null ? null : dto.getToUserId();
        if (toUserId == null || toUserId.equals(task.getAssigneeUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择有效的转办对象");
        }
        SysUser target = userMapper.selectById(toUserId);
        if (target == null || target.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "转办对象账号不可用");
        }
        taskMapper.markTransfer(taskId,
                "转办给 " + loadUserName(toUserId), now());
        FlowTask newTask = new FlowTask();
        newTask.setInstanceId(inst.getInstanceId());
        newTask.setNodeId(task.getNodeId());
        newTask.setNodeName(task.getNodeName());
        newTask.setAssigneeUserId(toUserId);
        newTask.setAssigneeName(loadUserName(toUserId));
        taskMapper.insert(newTask);
        addComment(inst.getInstanceId(), taskId, "TRANSFER",
                "转办给 " + loadUserName(toUserId) + (dto.getComment() == null ? "" : "：" + dto.getComment()));
        messageService.send(toUserId, 1, "转办待办：" + inst.getDefName(),
                "【" + task.getNodeName() + "】节点的审批已转办给您",
                "/flow/detail/" + inst.getInstanceId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void comment(Long instanceId, FlowHandleDTO dto) {
        FlowInstance inst = requireInstance(instanceId);
        checkReadAccess(inst);
        String content = dto == null ? null : dto.getContent();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "意见内容不能为空");
        }
        addComment(instanceId, null, "COMMENT", content);
    }

    @Override
    public int countTodo() {
        return taskMapper.countTodoByUser(UserContext.userId());
    }

    @Override
    public int countProcessing() {
        return instanceMapper.countProcessingByApplicant(UserContext.userId());
    }

    // ==================== 引擎内部 ====================

    private void advanceAfterApprove(FlowInstance inst, FlowTask task, String comment) {
        List<FlowNode> nodes = nodeMapper.selectByDefId(inst.getDefId());
        FlowNode node = findNode(nodes, task.getNodeId());
        Map<String, Object> data = readJson(inst.getFormDataJson());
        Long nextId = decideNextNode(node, data);
        if (nextId == null || isEndNode(findNode(nodes, nextId))) {
            finishApproved(inst, data);
            messageService.send(inst.getApplicantUserId(), 1,
                    "审批已通过：" + inst.getDefName(),
                    "《" + inst.getTitle() + "》已通过全部审批",
                    "/flow/detail/" + inst.getInstanceId());
        } else {
            inst.setCurrentNodeId(nextId);
            instanceMapper.updateCurrentNode(inst.getInstanceId(), nextId);
            createTask(inst, findNode(nodes, nextId), data);
        }
    }

    private void finishApproved(FlowInstance inst, Map<String, Object> data) {
        instanceMapper.updateStatus(inst.getInstanceId(), ST_APPROVED, now());
        try {
            for (FlowAttendanceNotifier notifier : attendanceNotifiers) {
                notifier.onApproved(inst, data);
            }
        } catch (Exception e) {
            log.error("考勤联动失败, instanceId={}", inst.getInstanceId(), e);
        }
    }

    private void createTask(FlowInstance inst, FlowNode node, Map<String, Object> formData) {
        Long assigneeId = resolveApprover(inst, node);
        if (assigneeId == null) {
            throw new BusinessException(ResultCode.ERROR, "无法确定【" + node.getNodeName() + "】的审批人");
        }
        FlowTask task = new FlowTask();
        task.setInstanceId(inst.getInstanceId());
        task.setNodeId(node.getNodeId());
        task.setNodeName(node.getNodeName());
        task.setAssigneeUserId(assigneeId);
        task.setAssigneeName(loadUserName(assigneeId));
        taskMapper.insert(task);
        messageService.send(assigneeId, 1, "待办审批：" + inst.getDefName(),
                "《" + inst.getTitle() + "》请在【" + node.getNodeName() + "】节点审批",
                "/flow/detail/" + inst.getInstanceId());
    }

    /** 处理人解析：ROLE / DEPT_LEADER / SPECIFIC_USER / OWN_DEPT（单一审批人模型） */
    private Long resolveApprover(FlowInstance inst, FlowNode node) {
        String type = node.getApproverType();
        List<Long> candidates = null;
        if ("ROLE".equals(type)) {
            candidates = userMapper.selectUserIdsByRole(node.getApproverIds());
        } else if ("DEPT_LEADER".equals(type)) {
            Long deptId = applicantDeptId(inst);
            candidates = deptId == null ? new ArrayList<>() : userMapper.selectDeptAdminUserIds(deptId);
            if (candidates.isEmpty()) {
                candidates = userMapper.selectUserIdsByRole("ADMIN");
            }
        } else if ("OWN_DEPT".equals(type)) {
            Long deptId = applicantDeptId(inst);
            candidates = deptId == null ? new ArrayList<>() : userMapper.selectUserIdsByDeptId(deptId);
            candidates = candidates.stream()
                    .filter(id -> !id.equals(inst.getApplicantUserId()))
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                candidates = userMapper.selectUserIdsByRole("ADMIN");
            }
        } else if ("SPECIFIC_USER".equals(type)) {
            candidates = new ArrayList<>();
            if (node.getApproverIds() != null) {
                for (String s : node.getApproverIds().split(",")) {
                    try {
                        candidates.add(Long.valueOf(s.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (candidates == null) {
            return null;
        }
        return candidates.stream()
                .filter(id -> {
                    SysUser u = userMapper.selectById(id);
                    return u != null && u.getStatus() == 1;
                })
                .findFirst()
                .orElse(null);
    }

    private Long applicantDeptId(FlowInstance inst) {
        if (inst.getApplicantEmpId() == null) {
            return null;
        }
        EmpEmployee emp = employeeMapper.selectById(inst.getApplicantEmpId());
        return emp == null ? null : emp.getDeptId();
    }

    /** 分支条件命中则走 branch_to_node_id，否则 next_node_id */
    private Long decideNextNode(FlowNode node, Map<String, Object> data) {
        if (StringUtils.hasText(node.getBranchField())
                && data.containsKey(node.getBranchField())
                && branchMatch(node, data.get(node.getBranchField()))) {
            return node.getBranchToNodeId();
        }
        return node.getNextNodeId();
    }

    private boolean branchMatch(FlowNode node, Object value) {
        String op = node.getBranchOp() == null ? "==" : node.getBranchOp();
        String expect = node.getBranchValue();
        if (value instanceof Number num) {
            double actual = num.doubleValue();
            try {
                double exp = Double.parseDouble(expect);
                return switch (op) {
                    case ">" -> actual > exp;
                    case ">=" -> actual >= exp;
                    case "<" -> actual < exp;
                    case "<=" -> actual <= exp;
                    default -> actual == exp;
                };
            } catch (NumberFormatException e) {
                return false;
            }
        }
        String actual = value == null ? "" : value.toString().trim();
        return switch (op) {
            case "IN", "contains" -> StringUtils.hasText(expect) && actual.contains(expect);
            case "!=" -> !actual.equals(expect);
            default -> actual.equals(expect);
        };
    }

    private void validateFields(List<FlowFormField> fields, Map<String, Object> data) {
        if (fields == null) {
            return;
        }
        for (FlowFormField f : fields) {
            Object value = data.get(f.getFieldKey());
            if (f.getRequired() != null && f.getRequired() == 1) {
                boolean empty = value == null
                        || (value instanceof String s && s.isBlank())
                        || (value instanceof List<?> l && l.isEmpty());
                if (empty) {
                    throw new BusinessException(ResultCode.PARAM_ERROR, "「" + f.getLabel() + "」为必填项");
                }
            }
        }
    }

    /** 将附件字段中本地上传的 fileId 回填到 instanceId 下 */
    private void rebindAttachments(List<FlowFormField> fields, Map<String, Object> data, Long instanceId) {
        if (fields == null) {
            return;
        }
        for (FlowFormField f : fields) {
            if (!"ATTACHMENT".equals(f.getFieldType())) {
                continue;
            }
            Object value = data.get(f.getFieldKey());
            collectFileIds(value).forEach(fileId ->
                    attachmentMapper.updateBizId(fileId, String.valueOf(instanceId)));
        }
    }

    private List<Long> collectFileIds(Object value) {
        List<Long> ids = new ArrayList<>();
        if (value instanceof Number num) {
            ids.add(num.longValue());
        } else if (value instanceof String s) {
            try {
                ids.add(Long.valueOf(s.trim()));
            } catch (NumberFormatException ignored) {
            }
        } else if (value instanceof List<?> list) {
            list.forEach(item -> ids.addAll(collectFileIds(item)));
        }
        return ids;
    }

    private List<AttachmentVO> loadAttachments(Long instanceId) {
        List<SysAttachment> list = attachmentMapper.selectByBiz("flow", String.valueOf(instanceId));
        return list.stream().map(a -> {
            AttachmentVO vo = new AttachmentVO();
            vo.setFileId(a.getFileId());
            vo.setOriginalName(a.getOriginalName());
            vo.setSize(a.getSize());
            vo.setDownloadUrl("/api/file/" + a.getFileId() + "/download");
            return vo;
        }).collect(Collectors.toList());
    }

    private void addComment(Long instanceId, Long taskId, String action, String content) {
        FlowComment comment = new FlowComment();
        comment.setInstanceId(instanceId);
        comment.setTaskId(taskId);
        comment.setUserId(UserContext.userId());
        comment.setUsername(UserContext.get().getUsername());
        comment.setAction(action);
        comment.setContent(content);
        commentMapper.insert(comment);
    }

    private FlowTask requireMinePending(Long taskId) {
        FlowTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审批任务不存在");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该任务已处理，请勿重复操作");
        }
        if (!task.getAssigneeUserId().equals(UserContext.userId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该任务不属于当前用户");
        }
        return task;
    }

    private FlowInstance requireInstance(Long instanceId) {
        FlowInstance inst = instanceMapper.selectById(instanceId);
        if (inst == null || inst.getDeleted() != null && inst.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "审批单不存在");
        }
        return inst;
    }

    private void checkReadAccess(FlowInstance inst) {
        UserPrincipal me = UserContext.get();
        if (me.isAdmin() || inst.getApplicantUserId().equals(me.getUserId())) {
            return;
        }
        boolean participated = taskMapper.selectByInstance(inst.getInstanceId()).stream()
                .anyMatch(t -> t.getAssigneeUserId().equals(me.getUserId()));
        if (!participated) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该审批单");
        }
    }

    private boolean isEndNode(FlowNode node) {
        return node == null || "END".equals(node.getNodeType());
    }

    private FlowNode findNode(List<FlowNode> nodes, Long nodeId) {
        return nodes.stream().filter(n -> n.getNodeId().equals(nodeId)).findFirst().orElse(null);
    }

    private String loadUserName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return String.valueOf(userId);
        }
        if (user.getEmpId() != null) {
            EmpEmployee emp = employeeMapper.selectById(user.getEmpId());
            if (emp != null && StringUtils.hasText(emp.getName())) {
                return emp.getName();
            }
        }
        return user.getUsername();
    }

    private String now() {
        return FMT.format(LocalDateTime.now());
    }

    private String writeJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "表单数据格式错误");
        }
    }

    private Map<String, Object> readJson(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}