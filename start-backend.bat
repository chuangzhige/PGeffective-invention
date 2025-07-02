@echo off
echo 启动CZ Admin后端服务...
echo.

cd cz-admin-backend

echo 检查Maven是否可用...
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请确保Maven已安装并配置在PATH中
    echo 请访问: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Maven版本:
mvn --version

echo.
echo 正在启动Spring Boot应用...
echo 服务将在 http://localhost:8080 启动
echo.
echo 正确的命令格式: mvn spring-boot:run (注意冒号)
echo 按 Ctrl+C 停止服务
echo.

mvn spring-boot:run 