# OA 办公管理系统

网络工程专业企业技术实习项目 · Spring Boot 3 + 原生 MyBatis + MySQL 8 + Vue 3 + Element Plus
前后端分离架构，管理员端 + 员工端双角色，用于验证数据中心网络环境下的业务系统部署。

## 项目结构

```
OA-System/
├── src/                    后端（Controller-Service-Mapper 三层）
├── oa-frontend/            前端（Vue3 <script setup> + Vite）
├── docs/
│   ├── 系统设计文档.md      架构 / 数据库 / 视觉设计方案
│   ├── 接口说明文档.md      RESTful API 全量说明
│   └── sql/
│       ├── oa_system.sql            建库建表 + 测试数据
│       └── postman_collection.json  Postman 接口集合
```

## 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 17+（23 亦可，已开启注解处理兼容） |
| Maven | 3.6+（仓库自带 mvnw） |
| MySQL | 8.0（root / root） |
| Node.js | 18+ |

## 启动方式

**一键启动（推荐）**：双击 `scripts\start-all.bat`，自动检查 Java/Node、首次自动建库、
并分别拉起前后端窗口。Git Bash 环境可用 `bash scripts/start-all.sh`（Ctrl+C 一并停止）。

单独启动：`scripts\start-backend.bat`（仅后端）、`scripts\start-frontend.bat`（仅前端，
自动补装依赖）。手动步骤如下。

### 1. 初始化数据库

```bash
mysql -uroot -proot --default-character-set=utf8mb4 < docs/sql/oa_system.sql
```

### 2. 启动后端（:8080）

```bash
./mvnw.cmd spring-boot:run
# 或打包运行：./mvnw.cmd -DskipTests package && java -jar target/OA-System-0.0.1-SNAPSHOT.jar
```

### 3. 启动前端（:5173）

```bash
cd oa-frontend
npm install
npm run dev
```

浏览器访问 <http://localhost:5173>，跨域已由后端全局 CORS 放行。

## 测试账号

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | 超级管理员 |
| wangqiang | 123456 | 部门管理员（网络运维组，可演示部门数据范围） |
| lina / zhangwei / chenjing | 123456 | 普通员工 |

## 核心功能（v2.0）

- **工作台门户**：待办/进行中/未读消息聚合、快捷入口、消息中心（审批/系统/公告提醒、已读标记、未读角标）、公告附件与已读统计、个人头像与操作记录
- **流程审批（核心）**：表单设计（文本/数字/日期/单选/多选/附件）、流程配置（串行节点 + 分支条件）、内置请假/加班/出差/物品领用模板、提交/同意/驳回/转办/撤回/补充意见、我发起的/待办/已办/归档查询、审批链路时间线
- **考勤管理**：上下班打卡（定位可选、迟到/早退/缺卡判定）、考勤规则配置（上下班时间/休息日/定位半径）、请假/加班审批通过自动联动考勤、月度考勤报表 Excel 兼容导出
- **组织通讯录**：按部门树展示（姓名/岗位/手机号），部门管理员限定本部门数据范围维护员工
- **文档中心**：多级目录、上传/下载/在线预览/重命名/删除、文件夹级授权（部门/人员 可读/可编辑）、覆盖上传保留历史版本、按文件名检索
- **系统基础管理**：三档角色（超管/部门管理员/员工）、操作日志（含登录、文件、流程、配置）、系统设置（消息推送开关、附件大小限制）
- **安全**：JWT 鉴权 + Token 自动续期、BCrypt 密码、`/api/admin` 与 `/api/dept-admin` 双层角色拦截（401/403）、部门数据范围过滤、前后端双重表单校验

## 接口自测（`scripts/`）

| 脚本 | 覆盖 |
|---|---|
| `flow-test.sh` | 流程：提交/分支/转办/驳回/撤回/待办/归档 |
| `att-test.sh` | 考勤：打卡/规则/报表/导出/审批联动 |
| `contact-test.sh` | 通讯录 + 部门管理员数据范围 |
| `doc-test.sh` | 文档：上传/覆盖版本/权限/检索/下载 |
| `portal-sys-test.sh` | 门户聚合/消息/公告附件与已读统计/角色管理/系统配置 |
| `target/api-test.sh` | v1 全量回归（42 用例） |

> 设计文档：《docs/系统设计文档.md》《docs/功能详解文档.md》《docs/技术实现文档.md》；数据库 v2 见 `docs/sql/oa_system.sql`（全量）与 `oa_v2_upgrade.sql`（v1 增量）。

## 密码转换工具（`scripts/password-tool.bat` / `.sh`）

BCrypt 为单向散列，密文不可直接还原明文；工具提供“反查”能力（用候选密码校验命中）：

```bash
password-tool hash 123456              # 明文 -> 密文
password-tool check 123456 '$2a$10$...' # 校验明文是否为该密文原密码
password-tool reverse '$2a$10$...'     # 候选字典反查单个密文
password-tool reverse-db               # 连库反查所有账号的明文
```

> 密文含 `$`，在 bash 中须用单引号包裹；Windows 下双击 `password-tool.bat` 按提示输入即可。
> 种子账号明文均为 `123456`，`reverse-db` 可直接命中。

## 接口自测

后端提供 Postman 集合（`docs/sql/postman_collection.json`），执行集合内“登录”请求后
Token 自动写入集合变量，其余接口直接可用。
