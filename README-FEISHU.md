# 飞书API集成使用说明

## 🎯 解决的问题

之前前端直接调用飞书API时遇到的**CORS跨域问题**已经通过后端代理完全解决。
白名单问题解决，不用配置白名单。

## 🚀 快速开始

### 1. 启动后端服务

<!-- **方法一：使用批处理脚本**
```bash
# 双击运行
start-backend.bat -->
```

**方法二：命令行启动**
```bash
cd cz-admin-backend
mvn spring-boot:run
```

**方法三：IDE运行**
- 打开 `cz-admin-backend/src/main/java/com/cz/admin/CzAdminBackendApplication.java`
- 运行main方法

### 2. 验证服务启动

访问以下链接确认服务正常：
- 健康检查：http://localhost:8080/api/feishu/health
- 配置信息：http://localhost:8080/api/feishu/config  
- 测试接口：http://localhost:8080/api/feishu/test

### 3. 使用调试工具

1. 启动前端项目：
```bash
cd cz-admin-master
npm run dev
```

2. 访问调试页面：http://localhost:5175/#/feishu/debug

3. 在调试页面：
   - 选择"使用后端代理（推荐）"
   - 点击"测试后端连接"
   - 点击"测试代理请求"

## 📡 API接口说明

### 后端代理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/feishu/test` | GET | 测试连接和配置 |
| `/api/feishu/tenant-access-token` | POST | 获取访问令牌 |
| `/api/feishu/user/{userId}` | GET | 获取用户信息 |
| `/api/feishu/attendance/records` | POST | 获取考勤记录 |
| `/api/feishu/health` | GET | 健康检查 |
| `/api/feishu/config` | GET | 获取配置信息 |

### 前端调用示例

```typescript
import { feishuAPI } from '@/utils/feishu';

// 测试连接
const result = await feishuAPI.testConnection();

// 获取令牌
const tokenResult = await feishuAPI.getTenantAccessToken();

// 获取用户信息
const userInfo = await feishuAPI.getUserInfoByOpenId('ou_xxxxxxxxx');

// 获取考勤记录
const attendance = await feishuAPI.getAttendanceRecords(
  'ou_xxxxxxxxx', 
  '2024-01-01', 
  '2024-01-31'
);
```

## ⚙️ 配置说明

### 飞书应用配置

配置文件：`cz-admin-backend/src/main/resources/application.yml`

```yaml
feishu:
  app-id: cli_a8b80b73f6bc5013
  app-secret: osgCKn8CUYNU3H14fY3X53RQfi5UkxKU
  base-url: https://open.feishu.cn
```

### 前端配置

配置文件：`cz-admin-master/src/config/feishu.ts`

```typescript
export const feishuConfig: FeishuConfig = {
  app_id: "cli_a8b80b73f6bc5013",
  app_secret: "osgCKn8CUYNU3H14fY3X53RQfi5UkxKU",
  base_url: "https://open.feishu.cn/"
};
```

## 🛠️ 故障排除

### 常见错误及解决方法

1. **无法连接后端服务**
   - 检查后端是否启动在 8080 端口
   - 确认防火墙没有阻挡端口

2. **Token获取失败**
   - 检查飞书应用配置是否正确
   - 确认网络连接正常
   - 验证App ID和App Secret

3. **CORS错误**
   - 确保使用后端代理而非直接调用飞书API
   - 在调试页面选择"使用后端代理"

4. **403/401权限错误**
   - 检查飞书应用权限配置
   - 确认应用已获得考勤相关权限

### 日志查看

后端日志会显示详细的API调用信息：
```
2024-01-01 10:00:00 [main] INFO  com.cz.admin.service.FeishuService - 获取新的tenant access token
2024-01-01 10:00:00 [main] INFO  com.cz.admin.service.FeishuService - 请求URL: https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal
2024-01-01 10:00:01 [main] INFO  com.cz.admin.service.FeishuService - 成功获取tenant access token，有效期: 7200 秒
```

## 🔧 开发说明

### 项目结构

```
cz-admin-backend/                          # 后端服务
├── src/main/java/com/cz/admin/
│   ├── config/
│   │   ├── FeishuConfig.java             # 飞书配置类
│   │   └── CorsConfig.java               # 跨域配置
│   ├── controller/
│   │   └── FeishuController.java         # 飞书API控制器
│   ├── service/
│   │   └── FeishuService.java            # 飞书服务类
│   ├── entity/
│   │   └── User.java                     # 用户实体类
│   ├── repository/
│   │   └── UserRepository.java           # 用户数据访问层
│   └── CzAdminBackendApplication.java    # 启动类
├── src/main/resources/
│   └── application.yml                   # 应用配置文件
└── pom.xml                               # Maven依赖配置

cz-admin-master/                          # 前端应用
├── src/
│   ├── config/
│   │   ├── feishu.ts                     # 飞书前端配置
│   │   └── feishu.json                   # 飞书配置JSON
│   ├── utils/
│   │   ├── feishu.ts                     # 飞书API工具类
│   │   └── accessibility.ts              # 可访问性工具
│   ├── api/
│   │   ├── feishu.ts                     # 飞书API接口定义
│   │   └── attendance/                   # 考勤相关API
│   └── views/feishu/                     # 飞书功能页面
│       ├── debug/
│       │   └── index.vue                 # 调试页面
│       ├── group-members/
│       │   └── index.vue                 # 考勤组成员管理
│       ├── openid-query/
│       │   └── index.vue                 # OpenID查询
│       ├── user-query/
│       │   └── index.vue                 # 用户信息查询
│       ├── data-display/
│       │   └── index.vue                 # 数据展示
│       ├── attendance-overview/          # 考勤概览 (待实现)
│       ├── attendance-groups/            # 考勤组管理 (待实现)
│       ├── attendance-summary/           # 考勤汇总 (待实现)
│       └── README.md                     # 飞书功能说明
├── .env.local                            # 环境变量配置
└── package.json                          # 前端依赖配置
```

### Token缓存机制

后端服务自动缓存租户访问令牌：
- Token有效期内复用缓存
- 提前10分钟自动刷新
- 异常时自动重新获取

## 📚 参考资料

- [飞书开放平台文档](https://open.feishu.cn/document/)
- [考勤API文档](https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/attendance-v1/user_daily_shift/query)
- [Spring Boot文档](https://spring.io/projects/spring-boot)

## 🎉 完成

现在你可以：
1. 无CORS问题地调用飞书API
2. 通过调试页面测试所有功能
3. 在项目中使用飞书API获取用户和考勤数据

享受顺畅的飞书API集成体验！🚀 

### 功能模块说明

#### 后端功能 (cz-admin-backend)

**核心服务类**:
- `FeishuService.java`: 飞书API集成服务，包含所有飞书API调用逻辑
- `FeishuController.java`: REST API控制器，提供HTTP接口
- `FeishuConfig.java`: 飞书应用配置管理

**数据层**:
- `User.java`: 用户实体类，包含飞书用户信息字段
- `UserRepository.java`: 用户数据访问接口

**主要API接口**:
- `GET /api/feishu/test`: 测试飞书连接
- `POST /api/feishu/tenant-access-token`: 获取租户访问令牌
- `GET /api/feishu/user/{userId}`: 获取用户信息
- `GET /api/feishu/attendance/groups`: 获取考勤组列表
- `POST /api/feishu/attendance/groups/{groupId}/members`: 获取考勤组成员
- `POST /api/feishu/attendance/records`: 获取考勤记录
- `POST /api/feishu/attendance/results`: 获取考勤结果

#### 前端功能 (cz-admin-master)

**配置管理**:
- `src/config/feishu.ts`: 飞书前端配置
- `src/config/feishu.json`: 飞书配置JSON文件
- `.env.local`: 环境变量配置

**工具类**:
- `src/utils/feishu.ts`: 飞书API调用工具类
- `src/utils/accessibility.ts`: 可访问性工具函数

**API接口**:
- `src/api/feishu.ts`: 飞书API接口定义
- `src/api/attendance/`: 考勤相关API接口

**页面功能**:
- `debug/index.vue`: 飞书API调试页面
- `group-members/index.vue`: 考勤组成员管理页面
- `openid-query/index.vue`: OpenID查询页面
- `user-query/index.vue`: 用户信息查询页面
- `data-display/index.vue`: 数据展示页面

**待实现功能**:
- `attendance-overview/`: 考勤概览页面
- `attendance-groups/`: 考勤组管理页面
- `attendance-summary/`: 考勤汇总页面

### 技术栈

**后端**:
- Spring Boot 3.x
- Spring Data JPA
- H2/MySQL 数据库
- 飞书开放平台SDK

**前端**:
- Vue 3 + TypeScript
- Ant Design Vue
- Vite 构建工具
- Pinia 状态管理

### 开发指南

#### 环境准备

1. **后端环境**:
   ```bash
   # 确保Java 17+已安装
   java -version
   
   # 确保Maven已安装
   mvn -version
   ```

2. **前端环境**:
   ```bash
   # 确保Node.js 18+已安装
   node -version
   
   # 确保pnpm已安装
   pnpm -version
   ```

#### 快速启动

1. **启动后端服务**:
   ```bash
   cd cz-admin-backend
   mvn spring-boot:run
   ```

2. **启动前端服务**:
   ```bash
   cd cz-admin-master
   pnpm install
   pnpm dev
   ```

3. **访问应用**:
   - 前端: http://localhost:3000
   - 后端API: http://localhost:8080/api

#### 配置说明

1. **飞书应用配置**:
   - 在飞书开放平台创建应用
   - 获取 `app_id` 和 `app_secret`
   - 配置到 `application.yml` 或环境变量

2. **环境变量配置**:
   ```bash
   # 后端环境变量
   FEISHU_APP_ID=your_app_id
   FEISHU_APP_SECRET=your_app_secret
   
   # 前端环境变量 (.env.local)
   VITE_FEISHU_APP_ID=your_app_id
   VITE_FEISHU_APP_SECRET=your_app_secret
   VITE_BACKEND_URL=http://localhost:8080/api
   ```

#### 开发流程

1. **新增飞书API功能**:
   - 在 `FeishuService.java` 中添加服务方法
   - 在 `FeishuController.java` 中添加控制器接口
   - 在前端 `feishu.ts` 中添加API调用方法
   - 创建对应的Vue页面

2. **调试飞书API**:
   - 使用 `debug/index.vue` 页面测试API
   - 查看浏览器控制台和后端日志
   - 使用飞书开放平台文档验证API调用

3. **代码规范**:
   - 后端遵循Spring Boot最佳实践
   - 前端遵循Vue 3 + TypeScript规范
   - 使用ESLint和Prettier保持代码质量 