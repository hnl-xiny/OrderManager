#!/bin/bash
echo "========================================"
echo "   订单管理系统 - 后端启动脚本"
echo "========================================"
echo ""

cd "$(dirname "$0")"

echo "[1/3] 检查 Maven..."
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未检测到 Maven，请先安装 Maven 3.9+"
    exit 1
fi

echo "[2/3] 检查 Java..."
if ! command -v java &> /dev/null; then
    echo "[错误] 未检测到 Java，请先安装 JDK 21"
    exit 1
fi

echo "[3/3] 启动 Spring Boot 应用..."
echo ""
echo "启动中，请稍候..."
echo ""

mvn spring-boot:run
