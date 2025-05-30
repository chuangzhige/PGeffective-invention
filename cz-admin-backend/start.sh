#!/bin/bash

# CZ Admin Backend 启动脚本

echo "==================================="
echo "    CZ Admin Backend 启动脚本"
echo "==================================="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请先安装Java 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 显示Java版本
echo "Java版本:"
java -version

echo ""
echo "正在编译项目..."

# 清理并编译项目
mvn clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "正在启动应用..."
echo "应用地址: http://localhost:8080/api"
echo "API文档: http://localhost:8080/api/feishu/test"
echo ""
echo "按 Ctrl+C 停止应用"
echo ""

# 启动应用
mvn spring-boot:run 