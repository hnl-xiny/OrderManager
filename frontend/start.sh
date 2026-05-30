#!/bin/bash
echo "========================================"
echo "   订单管理系统 - 前端启动脚本"
echo "========================================"
echo ""

cd "$(dirname "$0")"

echo "[1/4] 检查 Node.js..."
if ! command -v node &> /dev/null; then
    echo "[错误] 未检测到 Node.js，请先安装 Node.js 18+"
    exit 1
fi

echo "[2/4] 安装依赖..."
npm install

if [ $? -ne 0 ]; then
    echo "[错误] 依赖安装失败"
    exit 1
fi

echo "[3/4] 启动开发服务器..."
echo ""
echo "前端服务启动在 http://localhost:5173"
echo "后端接口代理到 http://localhost:8080"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

npm run dev
