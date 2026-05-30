# 订单管理系统 - 快速启动指南

## 环境要求

- **JDK 21** (Spring Boot 3.2 需要 JDK 17+)
- **Maven 3.9+**
- **Node.js 18+** (推荐 20.x)
- **PostgreSQL 18** (或 14-16)
- **Redis 8.6** (或 6.x)

## 启动步骤

### 1. 数据库初始化

首先在 PostgreSQL 中创建数据库并导入初始化脚本：

```bash
# 登录 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE order_manager_db;

# 退出后执行初始化脚本
\q
psql -U postgres -d order_manager_db -f sql/init.sql
```

### 2. 启动后端

```bash
cd backend

# 方式一：使用脚本启动
# Windows
start.bat

# Linux/Mac
chmod +x start.sh
./start.sh

# 方式二：直接使用 Maven
mvn spring-boot:run
```

后端启动后运行在 http://localhost:8080

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后运行在 http://localhost:5173

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 普通用户 | user | 123456 |

## 验证启动成功

1. 访问 http://localhost:5173
2. 使用管理员账号登录
3. 进入"订单管理"页面
4. 如果能看到订单列表，说明启动成功

## 常见问题

### 1. 后端启动失败

检查：
- PostgreSQL 是否正在运行
- 数据库连接配置是否正确
- Redis 是否正在运行

### 2. 前端无法连接后端

检查：
- 后端是否正常启动
- Vite 代理配置是否正确
- 端口是否被占用

### 3. 数据库连接失败

修改 `backend/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_manager_db
    username: postgres    # 你的用户名
    password: postgres   # 你的密码
```

## API 测试

使用 curl 测试接口：

```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 获取订单列表
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 技术栈说明

- **前端**: React 18 + Redux Toolkit + Ant Design + Vite
- **后端**: Spring Boot 3.2 + Spring Security + JWT + MyBatis-Plus
- **数据库**: PostgreSQL 18 (UUID主键 + NUMERIC金额类型)
- **缓存**: Redis 8.6 (订单缓存 + 关联数据缓存)

## 项目结构

```
OrderManager/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/ordermanager/
│   │       ├── config/         # 配置类
│   │       ├── controller/      # REST 控制器
│   │       ├── dto/             # 数据传输对象
│   │       ├── entity/          # 实体类
│   │       ├── handler/         # 异常处理器
│   │       ├── mapper/          # MyBatis Mapper
│   │       ├── service/         # 业务逻辑
│   │       └── utils/           # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/             # XML 映射文件
│   │   └── application.yml     # 配置文件
│   └── sql/
│       └── init.sql            # 数据库初始化脚本
├── frontend/                    # React 前端
│   ├── src/
│   │   ├── components/         # 公共组件
│   │   ├── pages/              # 页面组件
│   │   ├── services/           # API 服务
│   │   └── store/              # Redux 状态管理
│   └── package.json
└── README.md
```
