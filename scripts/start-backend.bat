@echo off
chcp 65001 >nul
setlocal

REM ============================================================
REM  OA 办公管理系统 后端启动脚本  Spring Boot (端口 8080)
REM ============================================================

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%"

echo ==============================================
echo   OA 办公管理系统 - 后端 (:8080)
echo ==============================================

java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Java，请安装 JDK 17+ 并配置 PATH
    pause
    exit /b 1
)

REM 数据库连通性检查（仅提示，不自动建库——建库请用 start-all 或手动执行 SQL）
mysql -uroot -proot -e "USE oa_system" >nul 2>&1
if errorlevel 1 (
    echo [WARN] oa_system 数据库不存在或 MySQL 未运行，后端将启动失败。
    echo        可运行 scripts\start-all.bat 自动建库，或手动执行:
    echo        mysql -uroot -proot --default-character-set=utf8mb4 ^< docs\sql\oa_system.sql
    pause
)

if not exist "target\OA-System-0.0.1-SNAPSHOT.jar" (
    echo [*] 未找到打包产物，先执行 Maven 打包（首次会下载依赖，请耐心等待）...
    call mvnw.cmd -DskipTests package
    if errorlevel 1 (
        echo [ERROR] 后端打包失败
        pause
        exit /b 1
    )
)
echo [*] java -jar 启动后端 ...
java -jar target\OA-System-0.0.1-SNAPSHOT.jar

pause
endlocal
