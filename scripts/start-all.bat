@echo off
chcp 65001 >nul
setlocal

REM ============================================================
REM  OA 办公管理系统 一键启动脚本
REM  后端: Spring Boot (mvnw spring-boot:run)   (端口 8080)
REM  前端: Vue 3 + Vite (npm run dev)           (端口 5173)
REM  数据库: 本地 MySQL 8.0 服务（root/root），未建库时自动初始化
REM ============================================================

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo ==============================================
echo   OA 办公管理系统 一键启动
echo ==============================================

REM ---------- 1. 环境检查 ----------
echo.
echo [1/3] 检查环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Java，请安装 JDK 17+ 并配置 PATH
    pause
    exit /b 1
)
node -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Node.js，请安装 Node.js 18+ 并配置 PATH
    pause
    exit /b 1
)
echo   OK: java + node

REM ---------- 2. 数据库检查（首次自动建库） ----------
echo [2/3] 检查 MySQL 与 oa_system 数据库...
mysql -uroot -proot -e "USE oa_system" >nul 2>&1
if not errorlevel 1 (
    echo   OK: oa_system 已存在
    goto :deps
)
REM 库不存在，进一步判断是服务没起还是缺库
mysql -uroot -proot -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL 未运行或 root 密码不对。请启动 MySQL80 服务后重试
    echo         ^(Win+R 输入 services.msc，找到 MySQL 服务并启动^)
    pause
    exit /b 1
)
echo   [*] 首次运行，初始化数据库 docs\sql\oa_system.sql ...
mysql -uroot -proot --default-character-set=utf8mb4 < docs\sql\oa_system.sql
if errorlevel 1 (
    echo [ERROR] 数据库初始化失败，请检查 docs\sql\oa_system.sql 与 MySQL 状态
    pause
    exit /b 1
)
echo   OK: 数据库已初始化

:deps
REM ---------- 3. 前端依赖 + 启动 ----------
if not exist "oa-frontend\node_modules" (
    echo   [*] 首次运行，安装前端依赖 npm install ...
    pushd "oa-frontend"
    call npm install --no-audit --no-fund
    popd
    if errorlevel 1 (
        echo [ERROR] 前端依赖安装失败，请检查网络后重试
        pause
        exit /b 1
    )
)

REM ---------- 4. 后端打包（jar 缺失或不可执行时） + 启动 ----------
set "JAR=target\OA-System-0.0.1-SNAPSHOT.jar"
set "NEED_BUILD="
if not exist "%JAR%" (
    set "NEED_BUILD=1"
) else (
    REM 瘦 jar（repackage 未成功）不含 BOOT-INF/，java -jar 会报"没有主清单属性"
    jar tf "%JAR%" 2>nul | findstr /B /C:"BOOT-INF/" >nul 2>&1
    if errorlevel 1 set "NEED_BUILD=1"
)
if defined NEED_BUILD (
    if exist "%JAR%" (
        echo   [*] 检测到 jar 不可执行（缺 Spring Boot 启动清单），重新打包 ...
    ) else (
        echo   [*] 后端未打包，先执行 mvnw package ...
    )
    call mvnw.cmd -DskipTests package
    if errorlevel 1 (
        echo [ERROR] 后端打包失败。请确认已安装 JDK 17+，且没有后端实例占用 %JAR%
        pause
        exit /b 1
    )
)

echo [3/3] 启动服务（各开独立窗口）...
start "OA-Backend" cmd /k "cd /d "%ROOT_DIR%" && java -jar target\OA-System-0.0.1-SNAPSHOT.jar"
start "OA-Frontend" cmd /k "cd /d "%ROOT_DIR%\oa-frontend" && call npm run dev"

echo.
echo ==============================================
echo   服务已启动，请等待 10~30 秒就绪：
echo.
echo     前端页面:  http://localhost:5173
echo     后端 API:  http://localhost:8080
echo.
echo   测试账号: admin / 123456 （管理员）
echo             lina / 123456  （员工）
echo.
echo   停止: 直接关闭对应的服务窗口
echo ==============================================
echo.
pause
endlocal
