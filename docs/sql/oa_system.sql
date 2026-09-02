-- ============================================================
-- 企业级 OA 办公管理系统 数据库初始化脚本
-- MySQL 8.0 ｜ 数据库：oa_system
-- 执行：mysql -uroot -proot --default-character-set=utf8mb4 < oa_system.sql
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
-- 2. 员工表 emp_employee
-- ------------------------------------------------------------
CREATE TABLE emp_employee (
    emp_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '员工ID',
    emp_no      VARCHAR(20)  NOT NULL COMMENT '工号',
    name        VARCHAR(30)  NOT NULL COMMENT '姓名',
    gender      TINYINT      NOT NULL DEFAULT 1 COMMENT '性别 0女 1男',
    phone       VARCHAR(11)  NOT NULL COMMENT '手机号',
    email       VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    dept_id     BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    hire_date   DATE         NOT NULL COMMENT '入职日期',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '在职状态 1在职 0离职',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (emp_id),
    UNIQUE KEY uk_emp_no (emp_no),
    KEY idx_emp_name (name),
    KEY idx_emp_dept (dept_id)
) ENGINE = InnoDB COMMENT = '员工表';

-- ------------------------------------------------------------
-- 3. 用户表 sys_user
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    user_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(30)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    role        VARCHAR(20)  NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色 ADMIN/EMPLOYEE',
    emp_id      BIGINT       DEFAULT NULL COMMENT '关联员工ID',
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

-- 员工
INSERT INTO emp_employee (emp_id, emp_no, name, gender, phone, email, dept_id, hire_date, status) VALUES
(1, 'EMP20260001', '张伟',   1, '13800000001', 'zhangwei@tianyu.com',  6,  '2026-01-05', 1),
(2, 'EMP20260002', '李娜',   0, '13800000002', 'lina@tianyu.com',      7,  '2026-01-12', 1),
(3, 'EMP20260003', '王强',   1, '13800000003', 'wangqiang@tianyu.com', 8,  '2026-02-01', 1),
(4, 'EMP20260004', '刘洋',   1, '13800000004', 'liuyang@tianyu.com',   6,  '2026-02-14', 1),
(5, 'EMP20260005', '陈静',   0, '13800000005', 'chenjing@tianyu.com',  9,  '2026-03-02', 1),
(6, 'EMP20260006', '杨帆',   1, '13800000006', 'yangfan@tianyu.com',   10, '2026-03-18', 1),
(7, 'EMP20260007', '赵敏',   0, '13800000007', 'zhaomin@tianyu.com',   3,  '2026-04-01', 1),
(8, 'EMP20260008', '孙浩',   1, '13800000008', 'sunhao@tianyu.com',    4,  '2026-04-15', 0),
(9, 'EMP20260000', '系统管理员', 1, '13800000000', 'admin@tianyu.com',   5,  '2026-01-01', 1);

-- 用户：管理员 + 员工账号（密码 123456 的 BCrypt 密文）
INSERT INTO sys_user (user_id, username, password, role, emp_id, status) VALUES
(1, 'admin',  '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'ADMIN',    9, 1),
(2, 'zhangwei', '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 1, 1),
(3, 'lina',   '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 2, 1),
(4, 'wangqiang', '$2a$10$VMklSLE4HZHhju.O8UbI7uxLsngNw3d4vpGnvbZvfMhdlNAyJUev2', 'EMPLOYEE', 3, 1),
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
