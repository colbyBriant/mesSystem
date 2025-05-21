@echo off
chcp 65001
title MES System Server

:: 设置工作目录
cd /d "%~dp0"

:: 使用打包的 JRE
set JAVA_HOME=%~dp0jre
set PATH=%JAVA_HOME%\bin;%PATH%

:: 启动后端服务
echo 正在启动 MES System 服务...
"%JAVA_HOME%\bin\java" -jar server-0.0.1-SNAPSHOT.jar

exit /b %errorlevel%