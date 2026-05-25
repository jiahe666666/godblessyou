# 抽奖系统

基于 Vue 3 + Spring Boot 3 + MySQL + Redis + Docker Compose 的前后端分离抽奖系统，覆盖注册登录、邮箱验证、JWT 认证、抽奖发放、后台奖品管理和一键部署。

## 快速开始

```bash
git clone https://github.com/jiahe666666/godblessyou.git
cd godblessyou
cp .env.example .env
# 编辑 .env 填入你的 SMTP Relay 信息（QQ/163/SendGrid 等，端口 587/465）
vim .env
docker compose up -d --build
```

访问 http://localhost ，管理员账号 `admin / Admin@123456`（在 `.env` 中可改）。

**SMTP Relay 无需 25 端口**，配置好就能发验证邮件。不想配也可以直接跳过验证用演示模式登录。

## 项目结构

```
.
├── backend/    Spring Boot 3.2 + JPA + Security + JWT + Flyway
├── frontend/   Vue 3.5 + Vite 8 + Pinia + Element Plus
├── docker-compose.yml
├── setup.sh
└── .env.example
```

## 邮件配置

### SMTP Relay（推荐，无需 25 端口）

编辑 `.env` 填入你的 SMTP 信息：

```env
SMTP_HOST=smtp.qq.com
SMTP_PORT=587
SMTP_USER=your-email@qq.com
SMTP_PASS=授权码
SMTP_AUTH=true
SMTP_STARTTLS=true
```

| 服务商 | SMTP_HOST | PORT | AUTH | STARTTLS | 密码说明 |
|---|---|---|---|---|---|
| QQ邮箱 | smtp.qq.com | 587 | true | true | 授权码 |
| 163邮箱 | smtp.163.com | 465 | true | false | 授权码 |
| 阿里企业邮 | smtp.mxhichina.com | 465 | true | false | 登录密码 |
| SendGrid | smtp.sendgrid.net | 587 | true | true | API Key |
| AWS SES | email-smtp.us-east-1.amazonaws.com | 587 | true | true | SMTP Credentials |

### 内置 postfix（需要 25 端口）

```bash
# .env 中设为 SMTP_HOST=postfix
docker compose --profile mail up -d --build
```

## 已实现能力

- 用户注册、登录、刷新 Token、退出登录
- 邮箱验证与重新发送验证邮件
- JWT 无状态鉴权，Refresh Token Redis 存储与黑名单登出
- 抽奖大厅、抽奖历史、后台奖品管理
- Redis 用户级锁 + MySQL 库存扣减，避免抽奖并发超发
- Flyway 数据迁移
- 邮件发送失败落库 + 定时任务重试
- SMTP Relay 支持（587/465），不依赖 25 端口
- 前端演示模式兜底

## 本地开发

```bash
# 前端
cd frontend && npm install && npm run dev

# 后端（需 Java 17 + MySQL + Redis）
cd backend && mvn spring-boot:run
```

## 一键部署

```bash
chmod +x setup.sh
./setup.sh
```

脚本交互式引导配置域名、管理员账号和邮件模式。

## 运行说明

- 前端 Nginx 反向代理 `/api` 到后端
- 数据库迁移统一通过 Flyway 管理
- 日志按天滚动保留 30 天
- Docker 镜像自动构建，无需本地安装 Java/Node