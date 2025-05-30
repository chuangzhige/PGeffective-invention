# CZ Admin Backend - 飞书集成服务

本项目是CZ管理系统的飞书API集成后端服务，主要功能包括：

## 🚀 功能特性

- **飞书用户信息查询**: 支持通过OpenId获取用户详细信息
- **考勤数据查询**: 支持查询用户考勤流水和统计数据
- **跨域支持**: 解决前端直接调用飞书API的CORS问题
- **错误处理**: 完善的错误处理和日志记录
- **官方SDK**: 使用飞书官方Java SDK，确保稳定性和兼容性

## 📝 OpenId 使用指南

### 什么是OpenId？

OpenId是飞书中用户的唯一标识符，格式通常为：`ou_xxxxxxxxxxxxxxxxxxxxxxxx`

### 如何获取OpenId？

1. **通过飞书开放平台控制台**：
   - 登录飞书开放平台
   - 在用户管理中查看用户信息

2. **通过飞书API**：
   - 使用邮箱或手机号查询用户信息
   - 从返回结果中获取open_id字段

3. **通过用户授权**：
   - 用户通过飞书登录授权后获取

### API 接口说明

#### 1. 获取用户信息
```http
GET /feishu/user/{openId}
```

**示例**：
```bash
curl "http://localhost:8080/api/feishu/user/ou_abc123def456ghi789"
```

**响应示例**：
```json
{
  "success": true,
  "data": {
    "open_id": "ou_abc123def456ghi789",
    "name": "张三",
    "email": "zhangsan@company.com",
    "mobile": "13800138000",
    "department_ids": ["od_123456"],
    "status": {
      "is_activated": true
    }
  }
}
```

#### 2. 查询用户考勤流水
```http
POST /feishu/user/{openId}/attendance/flow
```

**参数**：
- `checkTimeFrom`: 开始时间戳（秒）
- `checkTimeTo`: 结束时间戳（秒）

**示例**：
```bash
curl -X POST "http://localhost:8080/api/feishu/user/ou_abc123def456ghi789/attendance/flow" \
  -F "checkTimeFrom=1704067200" \
  -F "checkTimeTo=1704153600"
```

#### 3. 查询用户考勤统计
```http
POST /feishu/user/{openId}/attendance/stats
```

**参数**：
- `checkDateFrom`: 开始日期（YYYYMMDD）
- `checkDateTo`: 结束日期（YYYYMMDD）

**示例**：
```bash
curl -X POST "http://localhost:8080/api/feishu/user/ou_abc123def456ghi789/attendance/stats" \
  -F "checkDateFrom=20240101" \
  -F "checkDateTo=20240107"
```

### 前端集成示例

在Vue.js项目中使用：

```typescript
import { feishuAPI } from '@/utils/feishu';

// 获取用户信息
const getUserInfo = async (openId: string) => {
  try {
    const response = await feishuAPI.getUserInfoByOpenId(openId);
    if (response.success) {
      console.log('用户信息:', response.data);
    }
  } catch (error) {
    console.error('获取失败:', error);
  }
};

// 查询考勤流水
const getAttendanceFlow = async (openId: string) => {
  const checkTimeFrom = Math.floor(Date.now() / 1000 - 7 * 24 * 3600).toString();
  const checkTimeTo = Math.floor(Date.now() / 1000).toString();
  
  try {
    const response = await feishuAPI.queryUserFlowByOpenId(openId, checkTimeFrom, checkTimeTo);
    if (response.success) {
      console.log('考勤流水:', response.data);
    }
  } catch (error) {
    console.error('查询失败:', error);
  }
};
```

## 🔧 配置说明

### 1. application.yml 配置

```yaml
feishu:
  app-id: cli_a8b80b73f6bc5013
  app-secret: ${FEISHU_APP_SECRET:osgCKn8CUYNU3H14fY3X53RQfi5UkxKU}
  base-url: https://open.feishu.cn
```

### 2. 环境变量

可以通过环境变量覆盖配置：

```bash
export FEISHU_APP_SECRET=your-app-secret
```

### 3. 前端环境变量

在 `.env` 文件中配置后端地址：

```bash
VITE_BACKEND_URL=http://localhost:8080/api
```

## 🚀 运行项目

### 1. 后端启动

```bash
# 进入后端目录
cd cz-admin-backend

# 使用Maven启动
mvn spring-boot:run

# 或使用启动脚本
./start.sh
```

### 2. 前端启动

```bash
# 进入前端目录
cd cz-admin-master

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 3. 访问应用

- 后端API: http://localhost:8080/api
- 前端页面: http://localhost:3000
- 飞书用户查询页面: http://localhost:3000/feishu/user-query

## 🔍 测试接口

### 测试连接

```bash
curl "http://localhost:8080/api/feishu/test"
```

### 获取配置信息

```bash
curl "http://localhost:8080/api/feishu/config"
```

## 📊 错误处理

API返回统一的响应格式：

```json
{
  "success": true/false,
  "code": "错误码",
  "message": "错误信息",
  "data": "响应数据",
  "requestId": "请求ID"
}
```

常见错误码：
- `1001`: 参数错误
- `1002`: 权限不足
- `1003`: 用户不存在
- `1004`: 网络错误

## 📝 日志查看

应用日志会输出到控制台，包含详细的API调用信息和错误堆栈。

## 🛠️ 技术栈

- **后端**: Spring Boot 3.2.0, Java 17
- **飞书SDK**: lark-open-sdk 1.0.0
- **构建工具**: Maven
- **前端**: Vue.js 3, TypeScript, Vite

## 📞 支持

如有问题，请检查：
1. 飞书应用配置是否正确
2. 网络连接是否正常
3. OpenId格式是否正确
4. 用户是否在应用范围内

## 🎯 功能特性

- ✅ 飞书考勤API代理
- ✅ 用户考勤流水查询
- ✅ 用户考勤统计查询
- ✅ 完整的错误处理和日志记录
- ✅ RESTful API接口
- ✅ 跨域支持

## 🛠️ 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **飞书开放平台 Java SDK 1.0.0**
- **Lombok**
- **Maven**

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+

### 启动服务

1. **使用启动脚本（推荐）**
   ```bash
   chmod +x start.sh
   ./start.sh
   ```

2. **手动启动**
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```

3. **打包运行**
   ```bash
   mvn clean package
   java -jar target/cz-admin-backend-1.0.0.jar
   ```

## 📋 API接口

服务启动后访问：`http://localhost:8080/api`

### 测试连接
```
GET /feishu/test
```

### 获取配置信息
```
GET /feishu/config
```

### 查询考勤流水
```
POST /feishu/attendance/flow
参数:
- checkTimeFrom: 开始时间戳
- checkTimeTo: 结束时间戳
- userIds: 用户ID列表（可选）
```

### 查询考勤统计
```
POST /feishu/attendance/stats
参数:
- checkDateFrom: 开始日期（YYYYMMDD）
- checkDateTo: 结束日期（YYYYMMDD）
- userIds: 用户ID列表（可选）
```

## 🔧 前端集成

前端需要修改环境变量配置：

```env
# .env.local
VITE_BACKEND_URL=http://localhost:8080/api
```

然后前端代码会自动调用后端API而不是直接调用飞书API。

## 📝 日志查看

应用启动后，在控制台可以看到详细的日志信息，包括：
- API请求和响应
- 错误信息
- 飞书SDK调用日志

## 🐛 常见问题

### 1. 启动失败
- 检查Java版本是否为17+
- 检查Maven是否正确安装
- 检查端口8080是否被占用

### 2. 飞书API调用失败
- 检查app_id和app_secret是否正确
- 检查网络连接
- 查看控制台日志获取详细错误信息

### 3. 前端无法连接后端
- 确认后端服务已启动
- 检查前端配置的后端地址
- 确认跨域配置正确

## 📖 更多信息

- [飞书开放平台文档](https://open.feishu.cn/document/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [飞书Java SDK文档](https://github.com/larksuite/oapi-sdk-java) 