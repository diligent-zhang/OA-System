#!/usr/bin/env bash
# ============================================================
#  OA 办公管理系统 一键启动（Git Bash / WSL / Linux）
#  后端: Spring Boot mvnw spring-boot:run  (端口 8080)
#  前端: Vue 3 + Vite npm run dev          (端口 5173)
#  数据库: 本地 MySQL 8.0（root/root），未建库时自动初始化
# ============================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "=============================================="
echo "  OA 办公管理系统 一键启动"
echo "=============================================="

# ---------- 1. 环境检查 ----------
command -v java >/dev/null 2>&1 || { echo "[ERROR] 未检测到 Java (JDK 17+)"; exit 1; }
command -v node >/dev/null 2>&1 || { echo "[ERROR] 未检测到 Node.js (18+)"; exit 1; }
echo "[1/3] 环境 OK: java + node"

# ---------- 2. 数据库检查（首次自动建库） ----------
if mysql -uroot -proot -e "USE oa_system" >/dev/null 2>&1; then
    echo "[2/3] oa_system 已存在"
else
    if ! mysql -uroot -proot -e "SELECT 1" >/dev/null 2>&1; then
        echo "[ERROR] MySQL 未运行或 root 密码不对，请先启动 MySQL 服务"
        exit 1
    fi
    echo "[2/3] 首次运行，初始化数据库 docs/sql/oa_system.sql ..."
    if ! mysql -uroot -proot --default-character-set=utf8mb4 < docs/sql/oa_system.sql; then
        echo "[ERROR] 数据库初始化失败"
        exit 1
    fi
fi

# ---------- 3. 打包 + 依赖 + 并行启动 ----------
# 说明: 项目路径含中文，spring-boot:run fork 出的 JVM 以命令行参数传绝对 classpath 会被
# GBK 截断（ClassNotFound）。改用已验证可靠的「mvnw package + java -jar」。
JAR="target/OA-System-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR" ]; then
    echo "[*] 未找到打包产物，先执行 Maven 打包 ..."
    if [ -n "$MSYSTEM" ]; then
        # MSYS 拉起的 cmd 无法解析裸命令名，须加 .\ 前缀
        cmd //c ".\\mvnw.cmd -DskipTests package" || { echo "[ERROR] 后端打包失败"; exit 1; }
    else
        ./mvnw -DskipTests package || { echo "[ERROR] 后端打包失败"; exit 1; }
    fi
fi

if [ ! -d "oa-frontend/node_modules" ]; then
    echo "[*] 首次运行，安装前端依赖 ..."
    (cd oa-frontend && npm install --no-audit --no-fund) || { echo "[ERROR] npm install 失败"; exit 1; }
fi

echo "[3/3] 启动服务 (Ctrl+C 停止全部)..."
trap 'echo; echo "[+] 正在停止服务..."; jobs -p | xargs -r kill 2>/dev/null; wait 2>/dev/null; exit 0' INT TERM

(cd "$ROOT_DIR" && java -jar "$JAR") &
(cd "$ROOT_DIR/oa-frontend" && npm run dev) &

echo "  后端 API:  http://localhost:8080"
echo "  前端页面:  http://localhost:5173"
echo "  测试账号:  admin/123456（管理员） lina/123456（员工）"

wait
