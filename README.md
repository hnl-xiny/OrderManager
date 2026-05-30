# 订单管理系统

基于 React + Spring Boot + PostgreSQL + Redis 的全栈订单管理平台，支持客户、设备、订单的完整 CRUD 操作与状态流转。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | React 18 + Redux Toolkit + Ant Design + Vite |
| 后端 | Spring Boot 3.2 + Spring Security + JWT + MyBatis-Plus |
| 数据库 | PostgreSQL 18（UUID 主键）|
| 缓存 | Redis 8.6 |

## 快速启动

详细步骤见 [QUICKSTART.md](./QUICKSTART.md)，核心流程：

1. 初始化数据库：`psql -U postgres -d order_manager_db -f backend/sql/init.sql`
2. 启动后端：`cd backend && mvn spring-boot:run`
3. 启动前端：`cd frontend && npm install && npm run dev`

**默认账号**：参见 `backend/src/main/resources/application.yml.example` 或数据库初始化脚本

## 功能特性

- **订单管理**：创建、编辑、审核、发货、完成、删除（软删除），支持分页/筛选/关键词搜索
- **客户管理**：客户信息的增删改查
- **设备管理**：设备档案与状态管理（正常 / 维修中 / 停用）
- **重复订单校验**：同一客户 + 同一设备当日不可重复下单
- **Redis 缓存**：订单列表缓存 10 分钟，详情缓存 1 小时，关联数据缓存 6 小时
- **权限控制**：JWT 认证，管理员与普通操作员角色分离

## 项目结构

```
OrderManager/
├── backend/
│   ├── src/main/java/com/ordermanager/
│   │   ├── controller/   # REST API
│   │   ├── service/      # 业务逻辑
│   │   ├── mapper/       # MyBatis-Plus 数据访问
│   │   ├── entity/       # 实体类
│   │   ├── dto/          # 数据传输对象
│   │   ├── config/       # Security / Redis 配置
│   │   └── handler/      # 全局异常处理 / UUID 类型转换
│   ├── src/main/resources/
│   │   ├── mapper/       # XML 映射文件
│   │   └── application.yml
│   └── sql/
│       ├── init.sql      # 数据库初始化
│       └── seed_data.sql # 种子数据
├── frontend/
│   ├── src/
│   │   ├── components/   # 公共组件
│   │   ├── pages/        # 页面
│   │   ├── store/        # Redux 状态管理
│   │   ├── services/     # API 服务层
│   │   └── routes/       # 路由配置
│   └── package.json
└── TECHNICAL_DOCS.md     # 技术文档与 AI 辅助说明
```

## 核心 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/orders` | 分页查询订单 |
| POST | `/api/orders` | 新增订单 |
| PUT | `/api/orders/:id` | 编辑订单 |
| PUT | `/api/orders/:id/status` | 更新订单状态 |
| DELETE | `/api/orders` | 删除订单（批量）|
| GET | `/api/orders/:id` | 订单详情 |
| GET | `/api/customers` | 客户列表 |
| GET | `/api/equipment` | 设备列表 |

## Redis 缓存键

| 键前缀 | 说明 | 过期时间 |
|--------|------|----------|
| `orders:page:*` | 订单列表分页 | 10 分钟 |
| `order:id:*` | 订单详情 | 1 小时 |
| `customers` | 客户列表 | 6 小时 |
| `equipment:normal` | 正常设备列表 | 6 小时 |
| `orders:check:*` | 重复校验 | 24 小时 |

## AI 辅助开发

本项目在开发过程中借助 AI 辅助完成了数据库表结构设计、Redis 缓存策略设计、后端业务逻辑与权限控制、前端状态管理与组件代码生成。所有 AI 生成的代码均经过人工审核与业务适配，详细说明见 [TECHNICAL_DOCS.md](./TECHNICAL_DOCS.md)。
