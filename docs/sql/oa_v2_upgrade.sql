-- ============================================================
-- OA 系统 v1 → v2 增量升级脚本（适用于已初始化 v1 的库）
-- MySQL 8.0 ｜ 数据库：oa_system
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 oa_system < oa_v2_upgrade.sql
-- 全新安装请直接使用 oa_system.sql（含全部 v2 结构）
-- ============================================================
USE oa_system;

-- ------------------------------------------------------------
-- 1. 既有表 ALTER
-- ------------------------------------------------------------
ALTER TABLE emp_employee ADD COLUMN position VARCHAR(30) NOT NULL DEFAULT '' COMMENT '岗位' AFTER dept_id;
ALTER TABLE sys_user ADD COLUMN avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像文件路径' AFTER emp_id;

-- ------------------------------------------------------------
-- 2. 新增表（IF NOT EXISTS，可重复执行）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_config (
    config_key   VARCHAR(50)  NOT NULL COMMENT '配置键',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description  VARCHAR(255) COMMENT '说明',
    PRIMARY KEY (config_key)
) ENGINE = InnoDB COMMENT = '系统配置表';

CREATE TABLE IF NOT EXISTS sys_op_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id     BIGINT       COMMENT '操作人用户ID',
    username    VARCHAR(30)  COMMENT '操作人账号',
    op_type     VARCHAR(30)  NOT NULL COMMENT '操作类型',
    biz_type    VARCHAR(30)  COMMENT '业务对象',
    biz_id      VARCHAR(40)  COMMENT '业务ID',
    detail      VARCHAR(500) COMMENT '操作详情',
    ip          VARCHAR(50)  COMMENT 'IP地址',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_ol_user (user_id),
    KEY idx_ol_time (create_time),
    KEY idx_ol_type (op_type)
) ENGINE = InnoDB COMMENT = '操作日志表';

CREATE TABLE IF NOT EXISTS sys_message (
    msg_id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    receiver_user_id BIGINT       NOT NULL COMMENT '接收人用户ID',
    msg_type         TINYINT      NOT NULL DEFAULT 2 COMMENT '类型 1审批提醒 2系统通知 3公告提醒',
    title            VARCHAR(100) NOT NULL COMMENT '标题',
    content          VARCHAR(500) COMMENT '内容',
    biz_link         VARCHAR(200) COMMENT '跳转链接',
    is_read          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (msg_id),
    KEY idx_msg_recv (receiver_user_id, is_read)
) ENGINE = InnoDB COMMENT = '站内消息表';

CREATE TABLE IF NOT EXISTS sys_attachment (
    file_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    biz_type      VARCHAR(20)  NOT NULL COMMENT '业务类型',
    biz_id        VARCHAR(40)  NOT NULL COMMENT '业务ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name   VARCHAR(100) NOT NULL COMMENT '存储文件名',
    path          VARCHAR(255) NOT NULL COMMENT '存储路径',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '大小字节',
    content_type  VARCHAR(120) COMMENT 'MIME类型',
    uploader_id   BIGINT       COMMENT '上传人用户ID',
    upload_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (file_id),
    KEY idx_att_biz (biz_type, biz_id)
) ENGINE = InnoDB COMMENT = '通用附件表';

CREATE TABLE IF NOT EXISTS sys_notice_attachment (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    notice_id     BIGINT       NOT NULL COMMENT '公告ID',
    file_id       BIGINT       NOT NULL COMMENT '附件file_id',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '大小字节',
    upload_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_na_notice (notice_id)
) ENGINE = InnoDB COMMENT = '公告附件表';

CREATE TABLE IF NOT EXISTS sys_notice_read (
    id        BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT   NOT NULL COMMENT '公告ID',
    user_id   BIGINT   NOT NULL COMMENT '阅读人用户ID',
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_nr (notice_id, user_id)
) ENGINE = InnoDB COMMENT = '公告阅读记录表';

CREATE TABLE IF NOT EXISTS flow_form (
    form_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '表单ID',
    form_name   VARCHAR(60)  NOT NULL COMMENT '表单名称',
    form_key    VARCHAR(40)  NOT NULL COMMENT '表单标识',
    remark      VARCHAR(255) COMMENT '备注',
    built_in    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_by   BIGINT       COMMENT '创建人用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (form_id)
) ENGINE = InnoDB COMMENT = '审批表单定义';

CREATE TABLE IF NOT EXISTS flow_form_field (
    field_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字段ID',
    form_id    BIGINT       NOT NULL COMMENT '所属表单ID',
    field_key  VARCHAR(40)  NOT NULL COMMENT '字段键',
    label      VARCHAR(50)  NOT NULL COMMENT '字段名称',
    field_type VARCHAR(20)  NOT NULL COMMENT '类型 TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/ATTACHMENT',
    options    VARCHAR(500) COMMENT '候选值JSON',
    required   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填',
    sort_no    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (field_id),
    KEY idx_ff_form (form_id)
) ENGINE = InnoDB COMMENT = '审批表单字段';

CREATE TABLE IF NOT EXISTS flow_def (
    def_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '流程ID',
    form_id     BIGINT      NOT NULL COMMENT '绑定表单ID',
    def_name    VARCHAR(60) NOT NULL COMMENT '流程名称',
    category    VARCHAR(30) NOT NULL DEFAULT 'custom' COMMENT '类别',
    built_in    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否内置',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (def_id),
    KEY idx_fd_form (form_id)
) ENGINE = InnoDB COMMENT = '流程定义';

CREATE TABLE IF NOT EXISTS flow_node (
    node_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    def_id           BIGINT       NOT NULL COMMENT '所属流程ID',
    node_name        VARCHAR(60)  NOT NULL COMMENT '节点名称',
    node_type        VARCHAR(20)  NOT NULL DEFAULT 'APPROVAL' COMMENT '节点类型 APPROVAL/END',
    sort_no          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    approver_type    VARCHAR(20)  COMMENT '审批人方式 ROLE/DEPT_LEADER/SPECIFIC_USER/OWN_DEPT',
    approver_ids     VARCHAR(200) COMMENT '审批人参数',
    branch_field     VARCHAR(40)  COMMENT '分支条件字段',
    branch_op        VARCHAR(10)  COMMENT '分支比较符',
    branch_value     VARCHAR(100) COMMENT '分支比较值',
    branch_to_node_id BIGINT      COMMENT '命中分支走向的节点ID',
    next_node_id     BIGINT       COMMENT '未命中分支走向的节点ID，0=结束',
    PRIMARY KEY (node_id),
    KEY idx_fn_def (def_id)
) ENGINE = InnoDB COMMENT = '流程节点';

CREATE TABLE IF NOT EXISTS flow_instance (
    instance_id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '实例ID',
    def_id            BIGINT      NOT NULL COMMENT '流程ID',
    form_id           BIGINT      NOT NULL COMMENT '表单ID',
    def_name          VARCHAR(60) NOT NULL COMMENT '流程名称',
    category          VARCHAR(30) NOT NULL DEFAULT 'custom' COMMENT '流程类别',
    title             VARCHAR(120) NOT NULL COMMENT '申请标题',
    applicant_user_id BIGINT      NOT NULL COMMENT '发起人用户ID',
    applicant_emp_id  BIGINT      COMMENT '发起人员工ID',
    applicant_name    VARCHAR(30) COMMENT '发起人姓名',
    form_data_json    TEXT        COMMENT '表单数据JSON',
    status            VARCHAR(20) NOT NULL DEFAULT 'APPROVING' COMMENT '状态',
    current_node_id   BIGINT      COMMENT '当前节点ID',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    finish_time       DATETIME    COMMENT '结束时间',
    deleted           TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (instance_id),
    KEY idx_fi_app (applicant_user_id),
    KEY idx_fi_status (status),
    KEY idx_fi_def (def_id)
) ENGINE = InnoDB COMMENT = '流程实例';

CREATE TABLE IF NOT EXISTS flow_task (
    task_id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    instance_id      BIGINT      NOT NULL COMMENT '实例ID',
    node_id          BIGINT      COMMENT '节点ID',
    node_name        VARCHAR(60) COMMENT '节点名称',
    assignee_user_id BIGINT      NOT NULL COMMENT '审批人用户ID',
    assignee_name    VARCHAR(30) COMMENT '审批人姓名',
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    comment          VARCHAR(500) COMMENT '处理意见',
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    handle_time      DATETIME    COMMENT '处理时间',
    PRIMARY KEY (task_id),
    KEY idx_ft_assignee (assignee_user_id, status),
    KEY idx_ft_instance (instance_id)
) ENGINE = InnoDB COMMENT = '审批任务';

CREATE TABLE IF NOT EXISTS flow_comment (
    comment_id  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    instance_id BIGINT       NOT NULL COMMENT '实例ID',
    task_id     BIGINT       COMMENT '任务ID',
    user_id     BIGINT       NOT NULL COMMENT '操作人用户ID',
    username    VARCHAR(30)  COMMENT '操作人账号',
    action      VARCHAR(20)  NOT NULL COMMENT '动作',
    content     VARCHAR(500) COMMENT '意见内容',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (comment_id),
    KEY idx_fc_inst (instance_id)
) ENGINE = InnoDB COMMENT = '审批链路记录';

CREATE TABLE IF NOT EXISTS att_rule (
    rule_id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_name     VARCHAR(60) NOT NULL COMMENT '规则名称',
    work_start    VARCHAR(5)  NOT NULL COMMENT '上班时间 HH:mm',
    work_end      VARCHAR(5)  NOT NULL COMMENT '下班时间 HH:mm',
    latitude      DOUBLE      COMMENT '打卡点纬度',
    longitude     DOUBLE      COMMENT '打卡点经度',
    radius_m      INT         NOT NULL DEFAULT 200 COMMENT '打卡半径米',
    location_check TINYINT    NOT NULL DEFAULT 0 COMMENT '是否启用定位校验',
    rest_days     VARCHAR(50) COMMENT '休息日JSON',
    enabled       TINYINT     NOT NULL DEFAULT 1 COMMENT '是否启用',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (rule_id)
) ENGINE = InnoDB COMMENT = '考勤规则表';

CREATE TABLE IF NOT EXISTS att_record (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    emp_id      BIGINT      NOT NULL COMMENT '员工ID',
    record_date DATE        NOT NULL COMMENT '考勤日期',
    clock_in    DATETIME    COMMENT '上班打卡时间',
    clock_out   DATETIME    COMMENT '下班打卡时间',
    status      VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态',
    source      VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '来源',
    remark      VARCHAR(200) COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_att_date (emp_id, record_date)
) ENGINE = InnoDB COMMENT = '考勤记录表';

CREATE TABLE IF NOT EXISTS doc_folder (
    folder_id   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父文件夹ID 0=根',
    name        VARCHAR(60) NOT NULL COMMENT '文件夹名称',
    create_by   BIGINT      COMMENT '创建人用户ID',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (folder_id),
    KEY idx_df_parent (parent_id)
) ENGINE = InnoDB COMMENT = '文档文件夹';

CREATE TABLE IF NOT EXISTS doc_folder_perm (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    folder_id  BIGINT      NOT NULL COMMENT '文件夹ID',
    perm_type  VARCHAR(10) NOT NULL COMMENT '权限类型 READ/EDIT',
    grant_type VARCHAR(10) NOT NULL COMMENT '授权对象类型 DEPT/USER',
    grant_id   BIGINT      NOT NULL COMMENT '授权对象ID',
    PRIMARY KEY (id),
    KEY idx_dfp_folder (folder_id)
) ENGINE = InnoDB COMMENT = '文档文件夹授权';

CREATE TABLE IF NOT EXISTS doc_file (
    file_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    folder_id     BIGINT       NOT NULL COMMENT '所属文件夹ID',
    name          VARCHAR(120) NOT NULL COMMENT '显示名',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name   VARCHAR(100) NOT NULL COMMENT '存储文件名',
    path          VARCHAR(255) NOT NULL COMMENT '存储路径',
    ext           VARCHAR(20)  COMMENT '扩展名',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '大小字节',
    content_type  VARCHAR(120) COMMENT 'MIME类型',
    uploader_id   BIGINT       COMMENT '上传人用户ID',
    uploader_name VARCHAR(30)  COMMENT '上传人姓名',
    version_num   INT          NOT NULL DEFAULT 1 COMMENT '当前版本号',
    upload_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (file_id),
    UNIQUE KEY uk_df_folder (folder_id, name),
    KEY idx_df_deleted (deleted)
) ENGINE = InnoDB COMMENT = '文档文件';

CREATE TABLE IF NOT EXISTS doc_file_version (
    version_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '版本ID',
    file_id       BIGINT       NOT NULL COMMENT '文件ID',
    version_no    INT          NOT NULL COMMENT '版本号',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name   VARCHAR(100) NOT NULL COMMENT '存储文件名',
    path          VARCHAR(255) NOT NULL COMMENT '存储路径',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '大小字节',
    uploader_id   BIGINT       COMMENT '上传人用户ID',
    uploader_name VARCHAR(30)  COMMENT '上传人姓名',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (version_id),
    KEY idx_dfv_file (file_id)
) ENGINE = InnoDB COMMENT = '文档文件版本';

-- ------------------------------------------------------------
-- 3. 种子数据（幂等：先删后插 / INSERT IGNORE）
-- ------------------------------------------------------------
INSERT IGNORE INTO sys_config (config_key, config_value, description) VALUES
('message_push_enabled', 'true', '消息推送总开关 true/false'),
('attachment_max_size', '20', '附件/头像大小上限MB');

INSERT IGNORE INTO att_rule (rule_id, rule_name, work_start, work_end, location_check, rest_days, enabled) VALUES
(1, '默认考勤规则', '09:00', '18:00', 0, '["6","7"]', 1);

INSERT IGNORE INTO doc_folder (folder_id, parent_id, name, create_by) VALUES
(1, 0, '公共文档', 1);

-- 预置审批模板（见 oa_system.sql 注释说明，v1 库如已删除可在此重建）
INSERT IGNORE INTO flow_form (form_id, form_name, form_key, remark, built_in, status, create_by) VALUES
(1, '请假申请',   'LEAVE',       '请假审批表单', 1, 1, 1),
(2, '加班申请',   'OVERTIME',    '加班审批表单', 1, 1, 1),
(3, '出差申请',   'TRAVEL',      '出差审批表单', 1, 1, 1),
(4, '物品领用申请', 'REQUISITION', '物品领用审批表单', 1, 1, 1);

INSERT IGNORE INTO flow_form_field (form_id, field_key, label, field_type, options, required, sort_no) VALUES
(1, 'leave_type', '请假类型', 'SELECT', '["事假","病假","年假","调休"]', 1, 1),
(1, 'begin_date', '开始日期', 'DATE', NULL, 1, 2),
(1, 'end_date',   '结束日期', 'DATE', NULL, 1, 3),
(1, 'days',       '请假天数', 'NUMBER', NULL, 1, 4),
(1, 'reason',     '请假事由', 'TEXT', NULL, 1, 5),
(1, 'attachment', '证明材料', 'ATTACHMENT', NULL, 0, 6),
(2, 'work_date',  '加班日期', 'DATE', NULL, 1, 1),
(2, 'start_time', '开始时间', 'TEXT', NULL, 1, 2),
(2, 'end_time',   '结束时间', 'TEXT', NULL, 1, 3),
(2, 'hours',      '加班时长', 'NUMBER', NULL, 1, 4),
(2, 'reason',     '加班事由', 'TEXT', NULL, 1, 5),
(3, 'city',       '出差城市', 'TEXT', NULL, 1, 1),
(3, 'begin_date', '开始日期', 'DATE', NULL, 1, 2),
(3, 'end_date',   '结束日期', 'DATE', NULL, 1, 3),
(3, 'days',       '出差天数', 'NUMBER', NULL, 1, 4),
(3, 'reason',     '出差事由', 'TEXT', NULL, 1, 5),
(3, 'attachment', '相关附件', 'ATTACHMENT', NULL, 0, 6),
(4, 'item_name',  '物品名称', 'TEXT', NULL, 1, 1),
(4, 'quantity',   '数量',    'NUMBER', NULL, 1, 2),
(4, 'reason',     '领用用途', 'TEXT', NULL, 1, 3),
(4, 'attachment', '佐证材料', 'ATTACHMENT', NULL, 0, 4);

INSERT IGNORE INTO flow_def (def_id, form_id, def_name, category, built_in, status) VALUES
(1, 1, '请假申请',    'leave',       1, 1),
(2, 2, '加班申请',    'overtime',    1, 1),
(3, 3, '出差申请',    'travel',      1, 1),
(4, 4, '物品领用申请', 'requisition', 1, 1);

INSERT IGNORE INTO flow_node (node_id, def_id, node_name, node_type, sort_no, approver_type, approver_ids,
                              branch_field, branch_op, branch_value, branch_to_node_id, next_node_id) VALUES
(1,  1, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, 'days', '>', '3', 2, 3),
(2,  1, '人事负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 3),
(3,  1, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0),
(4,  2, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 5),
(5,  2, '结束',              'END',      2, NULL, NULL, NULL, NULL, NULL, NULL, 0),
(6,  3, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 7),
(7,  3, '行政负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 8),
(8,  3, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0),
(9,  4, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 10),
(10, 4, '行政负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 11),
(11, 4, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0);

-- 回填已有员工的岗位（可选）
UPDATE emp_employee SET position = '普通员工' WHERE position = '';

-- 演示部门管理员：网络运维组 wangqiang 晋升为部门管理员
UPDATE sys_user SET role = 'DEPT_ADMIN' WHERE username = 'wangqiang' AND role = 'EMPLOYEE';