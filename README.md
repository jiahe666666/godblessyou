# 抽奖系统

基于 `Vue 3 + Spring Boot 3 + MySQL + Redis + Postfix + Docker Compose` 的前后端分离抽奖系统示例，覆盖注册登录、邮箱验证、JWT 认证、抽奖发放、后台奖品管理和一键部署。

## 项目结构

```text
.
├─ backend/    Spring Boot 3.2 + JPA + Security + JWT + Flyway
├─ frontend/   Vue 3.5 + Vite 8 + Pinia + Element Plus
├─ docker-compose.yml
├─ setup.sh
└─ .env.example
```

## 已实现能力

- 用户注册、登录、刷新 Token、退出登录
- 邮箱验证与重新发送验证邮件
- JWT 无状态鉴权，Refresh Token Redis 存储与黑名单登出
- 抽奖大厅、抽奖历史、后台奖品管理
- Redis 用户级锁 + MySQL 库存扣减，避免抽奖并发超卖
- Flyway 数据迁移
- 邮件发送失败落库，并通过定时任务重试
- Docker Compose 一键部署基础文件

## 本地开发

### 前端

```bash
cd frontend
npm install
npm run dev
```

默认访问地址通常为 [http://localhost:5173](http://localhost:5173)。如果端口被占用，Vite 会自动切换到下一个空闲端口。

### 后端

项目依赖 Java 17、MySQL 8、Redis 7。本仓库当前未附带 Maven Wrapper，请确保本机已安装 Maven。

```bash
cd backend
mvn spring-boot:run
```

后端默认端口为 `8080`，Swagger 地址为 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)。

## 关键环境变量

后端配置定义在 `backend/src/main/resources/application.yml`，常用环境变量如下：

```env
DB_HOST=mysql
DB_PORT=3306
DB_NAME=lottery_system
DB_USERNAME=lottery
DB_PASSWORD=lottery123
REDIS_HOST=redis
REDIS_PORT=6379
SMTP_HOST=postfix
SMTP_PORT=587
SMTP_USER=noreply@example.com
SMTP_PASS=changeit
FRONTEND_BASE_URL=http://localhost
ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174
JWT_SECRET=replace-with-strong-secret
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=Admin@123456
LOTTERY_DAILY_LIMIT=10
LOTTERY_COOLDOWN_SECONDS=5
```

## 一键部署

### Linux / WSL

```bash
chmod +x setup.sh
./setup.sh
```

脚本会：

- 交互式收集域名、管理员账号和 Postfix 域名
- 生成 `.env`
- 执行 `docker compose up -d --build`
- 输出前端地址与管理后台地址

### 手动部署

```bash
cp .env.example .env
docker compose up -d --build
```

## 运行说明

- 前端生产镜像由 `frontend/Dockerfile` 构建，Nginx 配置位于 `frontend/nginx/default.conf`
- 后端生产镜像由 `backend/Dockerfile` 构建
- 日志目录默认写入 `/logs`，并通过 `logback-spring.xml` 按天滚动，保留 30 天
- 数据库初始化与结构变更统一通过 `backend/src/main/resources/db/migration` 管理

## 当前注意点

- `docker compose` 与 `mvn` 需要本机或目标环境提前安装
- `postfix` 目前按本地容器开发场景提供基础发信能力，如需真实公网发信，建议继续补 SPF、DKIM、TLS 和鉴权配置
- 前端保留了演示模式兜底逻辑，便于后端未启动时先看界面流程
