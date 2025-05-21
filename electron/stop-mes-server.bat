@echo off
chcp 65001
title 停止 MES System 服务

:: 查找并终止 Java 进程
echo 正在停止 MES System 服务...
taskkill /F /IM java.exe
exit /b 0