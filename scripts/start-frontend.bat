@echo off
chcp 65001 >nul
setlocal

REM ============================================================
REM  OA 办公管理系统 前端启动脚本  Vue 3 + Vite (端口 5173)
REM ============================================================

set "ROOT_DIR=%~dp0.."
cd /d "%ROOT_DIR%\oa-frontend"

echo ==============================================
echo   OA 办公管理系统 - 前端 (:5173)
echo ==============================================

node -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Node.js，请安装 Node.js 18+ 并配置 PATH
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo [*] 首次运行，安装依赖 npm install ...
    call npm install --no-audit --no-fund
    if errorlevel 1 (
        echo [ERROR] 依赖安装失败，请检查网络后重试
        pause
        exit /b 1
    )
)

echo [*] npm run dev ...（需后端已在 :8080 运行）
call npm run dev

pause
endlocal
