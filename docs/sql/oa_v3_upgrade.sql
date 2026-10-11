-- ============================================================
-- OA 系统 v3 增量升级脚本（在 v2 基础库上执行）
-- 内容：
--   1) 请假 / 出差表单：开始、结束改为日期时间(DATETIME)，时长改为自动计算(DURATION)
--   2) 新增即时通讯（会话 / 成员 / 消息）三张表
-- 幂等：可重复执行
-- ============================================================

-- ------------------------------------------------------------
-- 1) 表单字段：开始/结束时间精确到小时，时长由起止时间自动计算
-- ------------------------------------------------------------
UPDATE flow_form_field
SET field_type = 'DATETIME', label = '开始时间'
WHERE form_id = 1 AND field_key = 'begin_date';
UPDATE flow_form_field
SET field_type = 'DATETIME', label = '结束时间'
WHERE form_id = 1 AND field_key = 'end_date';
UPDATE flow_form_field
SET field_type = 'DURATION', label = '请假时长(天)',
    options = '{"from":"begin_date","to":"end_date"}'
WHERE form_id = 1 AND field_key = 'days';

UPDATE flow_form_field
SET field_type = 'DATETIME', label = '开始时间'
WHERE form_id = 3 AND field_key = 'begin_date';
UPDATE flow_form_field
SET field_type = 'DATETIME', label = '结束时间'
WHERE form_id = 3 AND field_key = 'end_date';
UPDATE flow_form_field
SET field_type = 'DURATION', label = '出差时长(天)',
    options = '{"from":"begin_date","to":"end_date"}'
WHERE form_id = 3 AND field_key = 'days';

-- ------------------------------------------------------------
-- 2) 即时通讯：会话表
--    type: SINGLE 单聊 / GROUP 群聊
--    single_key: 单聊去重键（较小的userId-较大的userId），群聊为 NULL
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversation (
    conversation_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    type            VARCHAR(10) NOT NULL DEFAULT 'SINGLE' COMMENT '类型 SINGLE/GROUP',
    group_name      VARCHAR(60) COMMENT '群名称（群聊）',
    single_key      VARCHAR(40) COMMENT '单聊去重键 minUserId-maxUserId，群聊为NULL',
    create_by       BIGINT      COMMENT '创建人用户ID',
    last_msg_id     BIGINT      COMMENT '最后一条消息ID',
    last_time       DATETIME    COMMENT '最后消息时间',
    create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (conversation_id),
    UNIQUE KEY uk_chat_single (single_key)
) ENGINE = InnoDB COMMENT = '即时通讯会话表';

-- ------------------------------------------------------------
-- 3) 即时通讯：会话成员表
--    last_read_msg_id: 该成员已读到的最大消息ID，用于计算未读数
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversation_member (
    id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'ID',
    conversation_id  BIGINT   NOT NULL COMMENT '会话ID',
    user_id          BIGINT   NOT NULL COMMENT '成员用户ID',
    last_read_msg_id BIGINT   NOT NULL DEFAULT 0 COMMENT '已读到的消息ID',
    join_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_member (conversation_id, user_id),
    KEY idx_chat_member_user (user_id)
) ENGINE = InnoDB COMMENT = '会话成员表';

-- ------------------------------------------------------------
-- 4) 即时通讯：消息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_message (
    msg_id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT        NOT NULL COMMENT '会话ID',
    sender_user_id  BIGINT        NOT NULL COMMENT '发送人用户ID',
    msg_type        VARCHAR(10)   NOT NULL DEFAULT 'TEXT' COMMENT '消息类型 TEXT',
    content         VARCHAR(2000) NOT NULL COMMENT '消息内容',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (msg_id),
    KEY idx_chat_msg_conv (conversation_id, msg_id)
) ENGINE = InnoDB COMMENT = '聊天消息表';
