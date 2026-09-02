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
| admin | 123456 | 管理员 |
| lina / zhangwei / wangqiang / chenjing | 123456 | 普通员工 |

## 核心功能

- **管理员端**：仪表盘（统计卡 + 部门人员饼图 + 最新公告）、员工管理（多条件分页 / CRUD / 批量删除 / 重置密码 / 账号启停）、部门管理（两级树 / 非空禁删）、公告管理（发布 / 置顶 / 上下架）
- **员工端**：个人工作台、公告中心（仅已发布）、个人信息维护、修改密码
- **安全**：JWT 鉴权 + Token 自动续期、BCrypt 密码、角色越权拦截（401/403）、前后端双重表单校验

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
