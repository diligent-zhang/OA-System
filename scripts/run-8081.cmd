@echo off
REM 从 target\classes + 依赖 classpath 启动第二实例（端口 8081），不触碰被占用的可执行 jar
cd /d "%~dp0.."
set /p CP=<target\cp.txt
java -cp "target\classes;%CP%" com.tianyu.oasystem.OaSystemApplication --server.port=8081
