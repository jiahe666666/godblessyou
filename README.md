# 抽奖系统

基于 Vue 3 + Spring Boot 3 + MySQL + Redis + Docker Compose 的前后端分离抽奖系统，覆盖注册登录、邮箱验证、JWT 认证、抽奖发放、后台奖品管理和一键部署。

## 项目结构

```
.
├── backend/    Spring Boot 3.2 + JPA + Security + JWT + Flyway
├── frontend/   Vue 3.5 + Vite 8 + Pinia + Element Plus
├── docker-compose.yml
├── setup.sh
└── .env.example
```

## 已实现能力

- 用户注册、登录、刷新 Token、退出登录
- 邮箱验证与重新发送验证邮件
- JWT 无状态鉴权，Refresh Token Redis 存储与黑名单登出
- 抽奖大厅、抽奖历史、后台奖品管理
- Redis 用户级锁 + MySQL 库存扣减，避免抽奖并发超发
- Flyway 数据迁移
- 邮件发送失败落库，并通过定时任务重试
- Docker Compose 一键部署基础文件

## 邮件配置（SMTP Relay）

本项目**不依赖 25 端口**，支持对接任意 SMTP Relay 服务。

### 方式一：外部 SMTP Relay（推荐，无需 25 端口）

在 `.env` 中配置你的 SMTP 服务商：

```env
# 以 QQ 邮箱为例
SMTP_HOST=smtp.qq.com
SMTP_PORT=587
SMTP_USER=your-email@qq.com
SMTP_PASS=授权码              # QQ邮箱 → 设置 → 账户 → POP3/SMTP → 生成授权码
SMTP_AUTH=true
SMTP_STARTTLS=true
```

常用 SMTP 配置速查：

| 服务商 | SMTP_HOST | SMTP_PORT | AUTH | STARTTLS | 密码说明 |
|---|---|---|---|---|---|
| QQ邮箱 | smtp.qq.com | 587 | true | true | 授权码（非登录密码） |
| 163邮箱 | smtp.163.com | 465 | true | false | 授权码 |
| 阿里企业邮 | smtp.mxhichina.com | 465 | true | false | 登录密码 |
| SendGrid | smtp.sendgrid.net | 587 | true | true | API Key |
| AWS SES | email-smtp.us-east-1.amazonaws.com | 587 | true | true | SMTP Credentials |

### 方式二：内置 postfix 容器（需要 25 端口）

```env
SMTP_HOST=postfix
SMTP_PORT=587
SMTP_USER=noreply@example.com
SMTP_PASS=changeit
SMTP_AUTH=false
SMTP_STARTTLS=false
POSTFIX_DOMAIN=example.com
```

启动时加 profile：

```bash
docker compose --profile mail up -d --build
```

## 本地开发

### 前端

```bash
cd frontend
npm install
npm run dev
```

默认访问地址通常为 http://localhost:5173 。如果端口被占用，Vite 会自动切换到下一个空闲端口。

### 后端

项目依赖 Java 17、MySQL 8、Redis 7。

```bash
cd backend
mvn spring-boot:run
```

后端默认端口为 `8080`，Swagger 地址为 http://localhost:8080/swagger-ui.html 。

## 一键部署

### Linux / WSL

```bash
chmod +x setup.sh
./setup.sh
```

脚本会：
- 交互式收集域名、管理员账号等信息
- 生成 `.env`
- 执行 `docker compose up -d --build`
- 输出前端地址与后台地址

### 手动部署

```bash
cp .env.example .env
# 编辑 .env 填入你的 SMTP 信息和管理员密码
vim .env
docker compose up -d --build
```

### 带 postfix 的部署

```bash
cp .env.example .env
# 编辑 .env 将 SMTP_HOST 改为 postfix
docker compose --profile mail up -d --build
```

## 运行说明

- 前端生产镜像由 `frontend/Dockerfile` 构建，Nginx 配置位于 `frontend/nginx/default.conf`
- 后端生产镜像由 `backend/Dockerfile` 构建
- 日志目录默认写入 `/logs`，按天滚动，保留 30 天
- 数据库初始化与结构变更统一通过 `backend/src/main/resources/db/migration` 管理
- 前端保留演示模式兜底逻辑，便于后端未启动时先看界面流程