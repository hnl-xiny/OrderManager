@echo off
chcp 65001 >nul
echo ========================================
echo   订单管理系统 - 后端启动脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] 检查 Maven...
mvn -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Maven，请先安装 Maven 3.9+
    pause
    exit /b 1
)

echo [2/3] 检查 Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Java，请先安装 JDK 21
    pause
    exit /b 1
)

echo [3/3] 启动 Spring Boot 应用...
echo.
echo 启动中，请稍候...
echo.

mvn spring-boot:run

pause
