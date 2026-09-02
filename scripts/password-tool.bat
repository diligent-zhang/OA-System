@echo off
chcp 65001 >nul
setlocal

REM ============================================================
REM  登录密码 明文/密文 转换工具（BCrypt 单向散列）
REM
REM  用法:
REM    password-tool hash <明文>
REM    password-tool check <明文> <密文>
REM    password-tool reverse <密文>
REM    password-tool reverse-db
REM
REM  注意: BCrypt 不可逆，"reverse" 仅用候选字典校验反查，
REM        只能命中常见密码或项目默认密码(123456)。
REM ============================================================

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"
set "JAVA_FILE=%SCRIPT_DIR%\PasswordTool.java"

REM ---------- 定位 spring-security-crypto 包（本地 Maven 仓库） ----------
set "CRYPTO_JAR="
for /f "delims=" %%j in ('dir /b /s "%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\*.jar" 2^>nul') do if not defined CRYPTO_JAR set "CRYPTO_JAR=%%j"

if not defined CRYPTO_JAR (
    echo [ERROR] 未找到 spring-security-crypto，请先执行 mvnw package 让 Maven 下载依赖
    pause
    exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 未检测到 Java，请安装 JDK 17+
    pause
    exit /b 1
)

REM ---------- 分派命令 ----------
if /i "%~1"=="reverse-db" (
    if not defined CRYPTO_JAR goto :eof
    mysql -uroot -proot --default-character-set=utf8mb4 -N -B -e "SELECT username, password FROM oa_system.sys_user" 2>nul | java -cp "%CRYPTO_JAR%" "%JAVA_FILE%" reverse-db
    if errorlevel 1 echo [ERROR] 数据库反查失败，请确认 MySQL 已启动且 root/root 可连
    pause
    goto :eof
)

java -cp "%CRYPTO_JAR%" "%JAVA_FILE%" %*
pause
endlocal
