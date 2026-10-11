-- ============================================================
-- 企业级 OA 办公管理系统 数据库初始化脚本（v2.0）
-- MySQL 8.0 ｜ 数据库：oa_system
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 < oa_system.sql
-- v2.0 新增：消息中心 / 公告附件与已读 / 操作日志 / 系统配置 / 审批流程 / 考勤 / 文档中心
-- ============================================================

DROP DATABASE IF EXISTS oa_system;
CREATE DATABASE oa_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE oa_system;

-- ------------------------------------------------------------
-- 1. 部门表 sys_dept
-- ------------------------------------------------------------
CREATE TABLE sys_dept (
    dept_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    dept_name   VARCHAR(50)  NOT NULL COMMENT '部门名称',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父部门ID，0为一级部门',
    sort_no     INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (dept_id),
    KEY idx_dept_parent (parent_id)
) ENGINE = InnoDB COMMENT = '部门表';

-- ------------------------------------------------------------
-- 2. 员工表 emp_employee（v2.0 新增 position 岗位）
-- ------------------------------------------------------------
CREATE TABLE emp_employee (
    emp_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    emp_no      VARCHAR(20)  NOT NULL COMMENT '工号',
    name        VARCHAR(30)  NOT NULL COMMENT '姓名',
    gender      TINYINT      NOT NULL DEFAULT 1 COMMENT '性别 0女 1男',
    phone       VARCHAR(11)  NOT NULL COMMENT '手机号',
    email       VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    dept_id     BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    position    VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '岗位',
    hire_date   DATE         NOT NULL COMMENT '入职日期',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '在职状态 1在职 0离职',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (emp_id),
    UNIQUE KEY uk_emp_no (emp_no),
    KEY idx_emp_name (name),
    KEY idx_emp_dept (dept_id)
) ENGINE = InnoDB COMMENT = '员工表';

-- ------------------------------------------------------------
-- 3. 用户表 sys_user（v2.0 角色扩展 DEPT_ADMIN；新增 avatar_url 头像）
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    user_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(30)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    role        VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色 ADMIN/DEPT_ADMIN/EMPLOYEE',
    emp_id      BIGINT       DEFAULT NULL COMMENT '关联员工ID',
    avatar_url  VARCHAR(255) DEFAULT NULL COMMENT '头像文件路径',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态 1正常 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    KEY idx_user_emp (emp_id)
) ENGINE = InnoDB COMMENT = '用户表';

-- ------------------------------------------------------------
-- 4. 公告表 sys_notice
-- ------------------------------------------------------------
CREATE TABLE sys_notice (
    notice_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title        VARCHAR(100) NOT NULL COMMENT '标题',
    content      TEXT         NOT NULL COMMENT '正文',
    type         TINYINT      NOT NULL DEFAULT 1 COMMENT '类型 1通知 2公告 3制度',
    top_flag     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1已发布 0已下架',
    publisher_id BIGINT       NOT NULL COMMENT '发布人用户ID',
    publish_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (notice_id),
    KEY idx_notice_time (publish_time),
    KEY idx_notice_publisher (publisher_id)
) ENGINE = InnoDB COMMENT = '公告表';

-- ------------------------------------------------------------
-- 5. v2.0 系统配置表 sys_config
-- ------------------------------------------------------------
CREATE TABLE sys_config (
    config_key   VARCHAR(50)  NOT NULL COMMENT '配置键',
    config_value VARCHAR(255) NOT NULL COMMENT '配置值',
    description  VARCHAR(255) COMMENT '说明',
    PRIMARY KEY (config_key)
) ENGINE = InnoDB COMMENT = '系统配置表';

-- ------------------------------------------------------------
-- 6. v2.0 操作日志表 sys_op_log
-- ------------------------------------------------------------
CREATE TABLE sys_op_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    user_id     BIGINT       COMMENT '操作人用户ID',
    username    VARCHAR(30)  COMMENT '操作人账号',
    op_type     VARCHAR(30)  NOT NULL COMMENT '操作类型 LOGIN/FLOW/FILE/NOTICE/DEPT/EMP/CONFIG/ATT/DOC/AUTH',
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

-- ------------------------------------------------------------
-- 7. v2.0 站内消息表 sys_message
-- ------------------------------------------------------------
CREATE TABLE sys_message (
    msg_id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    receiver_user_id BIGINT       NOT NULL COMMENT '接收人用户ID',
    msg_type         TINYINT      NOT NULL DEFAULT 2 COMMENT '类型 1审批提醒 2系统通知 3公告提醒',
    title            VARCHAR(100) NOT NULL COMMENT '标题',
    content          VARCHAR(500) COMMENT '内容',
    biz_link         VARCHAR(200) COMMENT '跳转链接',
    is_read          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (msg_id),
    KEY idx_msg_recv (receiver_user_id, is_read)
) ENGINE = InnoDB COMMENT = '站内消息表';

-- ------------------------------------------------------------
-- 8. v2.0 通用附件表 sys_attachment
-- ------------------------------------------------------------
CREATE TABLE sys_attachment (
    file_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '附件ID',
    biz_type      VARCHAR(20)  NOT NULL COMMENT '业务类型 notice/flow',
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

-- ------------------------------------------------------------
-- 9. v2.0 公告附件表 sys_notice_attachment
-- ------------------------------------------------------------
CREATE TABLE sys_notice_attachment (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    notice_id     BIGINT       NOT NULL COMMENT '公告ID',
    file_id       BIGINT       NOT NULL COMMENT '附件file_id',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '大小字节',
    upload_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_na_notice (notice_id)
) ENGINE = InnoDB COMMENT = '公告附件表';

-- ------------------------------------------------------------
-- 10. v2.0 公告已读记录表 sys_notice_read
-- ------------------------------------------------------------
CREATE TABLE sys_notice_read (
    id        BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    notice_id BIGINT   NOT NULL COMMENT '公告ID',
    user_id   BIGINT   NOT NULL COMMENT '阅读人用户ID',
    read_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_nr (notice_id, user_id)
) ENGINE = InnoDB COMMENT = '公告阅读记录表';

-- ------------------------------------------------------------
-- 11. v2.0 审批表单定义 flow_form
-- ------------------------------------------------------------
CREATE TABLE flow_form (
    form_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '表单ID',
    form_name   VARCHAR(60)  NOT NULL COMMENT '表单名称',
    form_key    VARCHAR(40)  NOT NULL COMMENT '表单标识 LEAVE/OVERTIME/TRAVEL/REQUISITION/自定义key',
    remark      VARCHAR(255) COMMENT '备注',
    built_in    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置 0否 1是',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_by   BIGINT       COMMENT '创建人用户ID',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (form_id)
) ENGINE = InnoDB COMMENT = '审批表单定义';

-- ------------------------------------------------------------
-- 12. v2.0 审批表单字段 flow_form_field
-- ------------------------------------------------------------
CREATE TABLE flow_form_field (
    field_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字段ID',
    form_id    BIGINT       NOT NULL COMMENT '所属表单ID',
    field_key  VARCHAR(40)  NOT NULL COMMENT '字段键',
    label      VARCHAR(50)  NOT NULL COMMENT '字段名称',
    field_type VARCHAR(20)  NOT NULL COMMENT '类型 TEXT/NUMBER/DATE/SELECT/MULTI_SELECT/ATTACHMENT',
    options    VARCHAR(500) COMMENT '候选值JSON，如 ["事假","病假"]',
    required   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否必填 0否 1是',
    sort_no    INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    PRIMARY KEY (field_id),
    KEY idx_ff_form (form_id)
) ENGINE = InnoDB COMMENT = '审批表单字段';

-- ------------------------------------------------------------
-- 13. v2.0 流程定义 flow_def
-- ------------------------------------------------------------
CREATE TABLE flow_def (
    def_id      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '流程ID',
    form_id     BIGINT      NOT NULL COMMENT '绑定表单ID',
    def_name    VARCHAR(60) NOT NULL COMMENT '流程名称',
    category    VARCHAR(30) NOT NULL DEFAULT 'custom' COMMENT '类别 leave/overtime/travel/requisition/custom',
    built_in    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否内置 0否 1是',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (def_id),
    KEY idx_fd_form (form_id)
) ENGINE = InnoDB COMMENT = '流程定义';

-- ------------------------------------------------------------
-- 14. v2.0 流程节点 flow_node
-- ------------------------------------------------------------
CREATE TABLE flow_node (
    node_id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '节点ID',
    def_id           BIGINT       NOT NULL COMMENT '所属流程ID',
    node_name        VARCHAR(60)  NOT NULL COMMENT '节点名称',
    node_type        VARCHAR(20)  NOT NULL DEFAULT 'APPROVAL' COMMENT '节点类型 APPROVAL/END',
    sort_no          INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    approver_type    VARCHAR(20)  COMMENT '审批人方式 ROLE/DEPT_LEADER/SPECIFIC_USER/OWN_DEPT',
    approver_ids     VARCHAR(200) COMMENT 'ROLE:角色值; SPECIFIC_USER:user_id逗号分隔',
    branch_field     VARCHAR(40)  COMMENT '分支条件字段',
    branch_op        VARCHAR(10)  COMMENT '分支比较符 >=/==/>/IN',
    branch_value     VARCHAR(100) COMMENT '分支比较值',
    branch_to_node_id BIGINT      COMMENT '命中分支走向的节点ID',
    next_node_id     BIGINT       COMMENT '未命中分支走向的节点ID，0=结束',
    PRIMARY KEY (node_id),
    KEY idx_fn_def (def_id)
) ENGINE = InnoDB COMMENT = '流程节点';

-- ------------------------------------------------------------
-- 15. v2.0 流程实例 flow_instance（审批单）
-- ------------------------------------------------------------
CREATE TABLE flow_instance (
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
    status            VARCHAR(20) NOT NULL DEFAULT 'APPROVING' COMMENT '状态 DRAFT/APPROVING/APPROVED/REJECTED/WITHDRAWN',
    current_node_id   BIGINT      COMMENT '当前节点ID',
    create_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    finish_time       DATETIME    COMMENT '结束时间',
    deleted           TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (instance_id),
    KEY idx_fi_app (applicant_user_id),
    KEY idx_fi_status (status),
    KEY idx_fi_def (def_id)
) ENGINE = InnoDB COMMENT = '流程实例';

-- ------------------------------------------------------------
-- 16. v2.0 审批任务 flow_task
-- ------------------------------------------------------------
CREATE TABLE flow_task (
    task_id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    instance_id      BIGINT      NOT NULL COMMENT '实例ID',
    node_id          BIGINT      COMMENT '节点ID',
    node_name        VARCHAR(60) COMMENT '节点名称',
    assignee_user_id BIGINT      NOT NULL COMMENT '审批人用户ID',
    assignee_name    VARCHAR(30) COMMENT '审批人姓名',
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态 PENDING/APPROVED/REJECTED/WITHDRAWN/TRANSFERRED',
    comment          VARCHAR(500) COMMENT '处理意见',
    create_time      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    handle_time      DATETIME    COMMENT '处理时间',
    PRIMARY KEY (task_id),
    KEY idx_ft_assignee (assignee_user_id, status),
    KEY idx_ft_instance (instance_id)
) ENGINE = InnoDB COMMENT = '审批任务';

-- ------------------------------------------------------------
-- 17. v2.0 审批链路 flow_comment
-- ------------------------------------------------------------
CREATE TABLE flow_comment (
    comment_id  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    instance_id BIGINT       NOT NULL COMMENT '实例ID',
    task_id     BIGINT       COMMENT '任务ID',
    user_id     BIGINT       NOT NULL COMMENT '操作人用户ID',
    username    VARCHAR(30)  COMMENT '操作人账号',
    action      VARCHAR(20)  NOT NULL COMMENT '动作 SUBMIT/APPROVE/REJECT/TRANSFER/WITHDRAW/COMMENT',
    content     VARCHAR(500) COMMENT '意见内容',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (comment_id),
    KEY idx_fc_inst (instance_id)
) ENGINE = InnoDB COMMENT = '审批链路记录';

-- ------------------------------------------------------------
-- 18. v2.0 考勤规则 att_rule
-- ------------------------------------------------------------
CREATE TABLE att_rule (
    rule_id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_name     VARCHAR(60) NOT NULL COMMENT '规则名称',
    work_start    VARCHAR(5)  NOT NULL COMMENT '上班时间 HH:mm',
    work_end      VARCHAR(5)  NOT NULL COMMENT '下班时间 HH:mm',
    latitude      DOUBLE      COMMENT '打卡点纬度',
    longitude     DOUBLE      COMMENT '打卡点经度',
    radius_m      INT         NOT NULL DEFAULT 200 COMMENT '打卡半径米',
    location_check TINYINT    NOT NULL DEFAULT 0 COMMENT '是否启用定位校验 0否 1是',
    rest_days     VARCHAR(50) COMMENT '休息日JSON如["6","7"]（0~6周日至周六）',
    enabled       TINYINT     NOT NULL DEFAULT 1 COMMENT '是否启用 0否 1是',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (rule_id)
) ENGINE = InnoDB COMMENT = '考勤规则表';

-- ------------------------------------------------------------
-- 19. v2.0 考勤记录 att_record
-- ------------------------------------------------------------
CREATE TABLE att_record (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    emp_id      BIGINT      NOT NULL COMMENT '员工ID',
    record_date DATE        NOT NULL COMMENT '考勤日期',
    clock_in    DATETIME    COMMENT '上班打卡时间',
    clock_out   DATETIME    COMMENT '下班打卡时间',
    status      VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态 NORMAL/LATE/EARLY_LEAVE/MISSING_IN/MISSING_OUT/LEAVE/OVERTIME/ABSENT',
    source      VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '来源 MANUAL手动/FLOW_LINK审批联动',
    remark      VARCHAR(200) COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_att_date (emp_id, record_date)
) ENGINE = InnoDB COMMENT = '考勤记录表';

-- ------------------------------------------------------------
-- 20. v2.0 文档文件夹 doc_folder
-- ------------------------------------------------------------
CREATE TABLE doc_folder (
    folder_id   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父文件夹ID 0=根',
    name        VARCHAR(60) NOT NULL COMMENT '文件夹名称',
    create_by   BIGINT      COMMENT '创建人用户ID',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (folder_id),
    KEY idx_df_parent (parent_id)
) ENGINE = InnoDB COMMENT = '文档文件夹';

-- ------------------------------------------------------------
-- 21. v2.0 文档文件夹授权 doc_folder_perm
-- ------------------------------------------------------------
CREATE TABLE doc_folder_perm (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    folder_id  BIGINT      NOT NULL COMMENT '文件夹ID',
    perm_type  VARCHAR(10) NOT NULL COMMENT '权限类型 READ/EDIT',
    grant_type VARCHAR(10) NOT NULL COMMENT '授权对象类型 DEPT/USER',
    grant_id   BIGINT      NOT NULL COMMENT '授权对象ID（部门ID/用户ID）',
    PRIMARY KEY (id),
    KEY idx_dfp_folder (folder_id)
) ENGINE = InnoDB COMMENT = '文档文件夹授权';

-- ------------------------------------------------------------
-- 22. v2.0 文档文件 doc_file
-- ------------------------------------------------------------
CREATE TABLE doc_file (
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
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    PRIMARY KEY (file_id),
    UNIQUE KEY uk_df_folder (folder_id, name),
    KEY idx_df_deleted (deleted)
) ENGINE = InnoDB COMMENT = '文档文件';

-- ------------------------------------------------------------
-- 23. v2.0 文档文件版本 doc_file_version
-- ------------------------------------------------------------
CREATE TABLE doc_file_version (
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

-- ============================================================
-- 初始化数据（测试密码统一 123456，BCrypt 密文）
-- ============================================================

-- 部门：一级 5 个 + 二级若干
INSERT INTO sys_dept (dept_id, dept_name, parent_id, sort_no) VALUES
(1, '技术部',       0, 1),
(2, '市场部',       0, 2),
(3, '人事部',       0, 3),
(4, '财务部',       0, 4),
(5, '行政部',       0, 5),
(6, '后端开发组',   1, 1),
(7, '前端开发组',   1, 2),
(8, '网络运维组',   1, 3),
(9, '市场推广组',   2, 1),
(10, '品牌策划组',  2, 2);

-- 员工（v2.0 增加岗位）
INSERT INTO emp_employee (emp_id, emp_no, name, gender, phone, email, dept_id, position, hire_date, status) VALUES
(1, 'EMP20260001', '张伟',   1, '13800000001', 'zhangwei@tianyu.com',  6,  '后端工程师',      '2026-01-05', 1),
(2, 'EMP20260002', '李娜',   0, '13800000002', 'lina@tianyu.com',      7,  '前端工程师',      '2026-01-12', 1),
(3, 'EMP20260003', '王强',   1, '13800000003', 'wangqiang@tianyu.com', 8,  '网络工程师',      '2026-02-01', 1),
(4, 'EMP20260004', '刘洋',   1, '13800000004', 'liuyang@tianyu.com',   6,  '后端工程师',      '2026-02-14', 1),
(5, 'EMP20260005', '陈静',   0, '13800000005', 'chenjing@tianyu.com',  9,  '市场专员',        '2026-03-02', 1),
(6, 'EMP20260006', '杨帆',   1, '13800000006', 'yangfan@tianyu.com',   10, '品牌策划',        '2026-03-18', 1),
(7, 'EMP20260007', '赵敏',   0, '13800000007', 'zhaomin@tianyu.com',   3,  '人事专员',        '2026-04-01', 1),
(8, 'EMP20260008', '孙浩',   1, '13800000008', 'sunhao@tianyu.com',    4,  '会计',            '2026-04-15', 0),
(9, 'EMP20260000', '系统管理员', 1, '13800000000', 'admin@tianyu.com',   5,  '系统管理员',      '2026-01-01', 1);

-- 用户：管理员 + 员工账号（密码 123456 的 BCrypt 密文）
INSERT INTO sys_user (user_id, username, password, role, emp_id, status) VALUES
(1, 'admin',  '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'ADMIN',    9, 1),
(2, 'zhangwei', '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 1, 1),
(3, 'lina',   '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 2, 1),
(4, 'wangqiang', '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'DEPT_ADMIN', 3, 1),
(5, 'chenjing', '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 5, 1);

-- 公告
INSERT INTO sys_notice (title, content, type, top_flag, status, publisher_id, publish_time) VALUES
('关于公司数据中心网络升级完成的通知',
 '各位同事：\n公司数据中心网络升级已于本周末顺利完成，服务器 VLAN 划分、VPN 远程访问与防火墙端口策略均已生效。升级后内网访问延迟明显降低，请大家如遇到访问异常及时反馈至网络运维组。\n\n——行政部',
 1, 1, 1, 1, '2026-08-25 09:00:00'),
('OA 办公管理系统正式上线公告',
 '为提升日常办公效率，公司 OA 办公管理系统正式上线。系统支持员工信息管理、部门管理、公告浏览等功能。管理员账号请联系人事部开通，普通员工可自行注册。',
 2, 1, 1, 1, '2026-08-26 10:30:00'),
('员工考勤与请假管理制度（2026 版）',
 '第一章 总则\n为规范公司考勤管理，营造良好工作秩序，制定本制度。\n第二章 考勤\n1. 工作时间为 9:00-18:00，午休 12:00-13:00；\n2. 每月允许两次 30 分钟内迟到；\n第三章 请假\n事假需提前一天在 OA 系统提交，病假需提供证明。',
 3, 0, 1, 1, '2026-08-28 14:00:00'),
('中秋节放假安排通知',
 '根据国家法定节假日安排，中秋节放假 3 天（9 月 25 日至 9 月 27 日），9 月 28 日正常上班。放假期间请各部门做好数据备份与机房巡检安排。',
 1, 0, 1, 1, '2026-09-01 08:30:00'),
('（草稿示例）国庆活动预告',
 '此公告尚未发布，用于演示下架/草稿状态。',
 1, 0, 0, 1, '2026-09-01 09:00:00');

-- 系统配置
INSERT INTO sys_config (config_key, config_value, description) VALUES
('message_push_enabled', 'true', '消息推送总开关 true/false'),
('attachment_max_size', '20', '附件/头像大小上限MB');

-- 考勤规则（默认）
INSERT INTO att_rule (rule_id, rule_name, work_start, work_end, location_check, rest_days, enabled) VALUES
(1, '默认考勤规则', '09:00', '18:00', 0, '["6","7"]', 1);

-- 文档根目录
INSERT INTO doc_folder (folder_id, parent_id, name, create_by) VALUES (1, 0, '公共文档', 1);
-- 根目录不设置显式授权：默认全员可读、仅管理员可编辑（代码兜底）

-- ============================================================
-- 审批流程预置模板（表单 + 字段 + 流程 + 节点）
-- ============================================================

-- 表单定义
INSERT INTO flow_form (form_id, form_name, form_key, remark, built_in, status, create_by) VALUES
(1, '请假申请',   'LEAVE',       '请假审批表单', 1, 1, 1),
(2, '加班申请',   'OVERTIME',    '加班审批表单', 1, 1, 1),
(3, '出差申请',   'TRAVEL',      '出差审批表单', 1, 1, 1),
(4, '物品领用申请', 'REQUISITION', '物品领用审批表单', 1, 1, 1);

-- 请假表单字段
INSERT INTO flow_form_field (form_id, field_key, label, field_type, options, required, sort_no) VALUES
(1, 'leave_type', '请假类型', 'SELECT', '["事假","病假","年假","调休"]', 1, 1),
(1, 'begin_date', '开始时间', 'DATETIME', NULL, 1, 2),
(1, 'end_date',   '结束时间', 'DATETIME', NULL, 1, 3),
(1, 'days',       '请假时长(天)', 'DURATION', '{"from":"begin_date","to":"end_date"}', 1, 4),
(1, 'reason',     '请假事由', 'TEXT', NULL, 1, 5),
(1, 'attachment', '证明材料', 'ATTACHMENT', NULL, 0, 6);

-- 加班表单字段
INSERT INTO flow_form_field (form_id, field_key, label, field_type, options, required, sort_no) VALUES
(2, 'work_date',  '加班日期', 'DATE', NULL, 1, 1),
(2, 'start_time', '开始时间', 'TEXT', NULL, 1, 2),
(2, 'end_time',   '结束时间', 'TEXT', NULL, 1, 3),
(2, 'hours',      '加班时长', 'NUMBER', NULL, 1, 4),
(2, 'reason',     '加班事由', 'TEXT', NULL, 1, 5);

-- 出差表单字段
INSERT INTO flow_form_field (form_id, field_key, label, field_type, options, required, sort_no) VALUES
(3, 'city',       '出差城市', 'TEXT', NULL, 1, 1),
(3, 'begin_date', '开始时间', 'DATETIME', NULL, 1, 2),
(3, 'end_date',   '结束时间', 'DATETIME', NULL, 1, 3),
(3, 'days',       '出差时长(天)', 'DURATION', '{"from":"begin_date","to":"end_date"}', 1, 4),
(3, 'reason',     '出差事由', 'TEXT', NULL, 1, 5),
(3, 'attachment', '相关附件', 'ATTACHMENT', NULL, 0, 6);

-- 物品领用表单字段
INSERT INTO flow_form_field (form_id, field_key, label, field_type, options, required, sort_no) VALUES
(4, 'item_name',  '物品名称', 'TEXT', NULL, 1, 1),
(4, 'quantity',   '数量',    'NUMBER', NULL, 1, 2),
(4, 'reason',     '领用用途', 'TEXT', NULL, 1, 3),
(4, 'attachment', '佐证材料', 'ATTACHMENT', NULL, 0, 4);

-- 流程定义
INSERT INTO flow_def (def_id, form_id, def_name, category, built_in, status) VALUES
(1, 1, '请假申请',    'leave',       1, 1),
(2, 2, '加班申请',    'overtime',    1, 1),
(3, 3, '出差申请',    'travel',      1, 1),
(4, 4, '物品领用申请', 'requisition', 1, 1);

-- 流程节点（branch_field 命中则走分支，否则走 next_node_id；next_node_id=0 表示结束）
-- 请假：部门负责人(分支: days>3 → 人事负责人) → 结束
INSERT INTO flow_node (node_id, def_id, node_name, node_type, sort_no, approver_type, approver_ids,
                       branch_field, branch_op, branch_value, branch_to_node_id, next_node_id) VALUES
(1, 1, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, 'days', '>', '3', 2, 3),
(2, 1, '人事负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 3),
(3, 1, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0),
-- 加班：部门负责人 → 结束
(4, 2, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 5),
(5, 2, '结束',              'END',      2, NULL, NULL, NULL, NULL, NULL, NULL, 0),
-- 出差：部门负责人 → 行政负责人 → 结束
(6, 3, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 7),
(7, 3, '行政负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 8),
(8, 3, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0),
-- 物品领用：部门负责人 → 行政负责人 → 结束
(9,  4, '直属部门负责人审批', 'APPROVAL', 1, 'DEPT_LEADER', NULL, NULL, NULL, NULL, NULL, 10),
(10, 4, '行政负责人审批',     'APPROVAL', 2, 'ROLE',        'ADMIN', NULL, NULL, NULL, NULL, 11),
(11, 4, '结束',              'END',      3, NULL, NULL, NULL, NULL, NULL, NULL, 0);