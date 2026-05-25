# 抽奖系统

基于 Vue 3 + Spring Boot 3 + MySQL + Redis + Docker Compose 的前后端分离抽奖系统，覆盖注册登录、邮箱验证、JWT 认证、抽奖发放、后台奖品管理和一键部署。

## 快速开始（零配置）

```bash
git clone https://github.com/jiahe666666/godblessyou.git
cd godblessyou
docker compose up -d --build
```

访问 http://localhost ，用 `admin / Admin@123456` 登录管理后台。

验证邮件在 http://localhost:8025 （MailHog 邮件查看器）里查看。

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

### 默认：MailHog（测试用，零配置）

无需任何 SMTP 账号，所有验证邮件自动捕获到 http://localhost:8025 ，点击即验证。

### 切换到真实 SMTP Relay

编辑 `.env`，改为你的 SMTP 服务商：

```env
SMTP_HOST=smtp.qq.com
SMTP_PORT=587
SMTP_USER=your-email@qq.com
SMTP_PASS=授权码
SMTP_AUTH=true
SMTP_STARTTLS=true
```

常用 SMTP 速查：

| 服务商 | SMTP_HOST | PORT | AUTH | STARTTLS |
|---|---|---|---|---|
| MailHog（默认） | mailhog | 1025 | false | false |
| QQ邮箱 | smtp.qq.com | 587 | true | true |
| 163邮箱 | smtp.163.com | 465 | true | false |
| 阿里企业邮 | smtp.mxhichina.com | 465 | true | false |
| SendGrid | smtp.sendgrid.net | 587 | true | true |
| AWS SES | email-smtp.us-east-1.amazonaws.com | 587 | true | true |

### 内置 postfix（需要 25 端口）

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

### 后端

需要 Java 17、MySQL 8、Redis 7 本地运行，或只启动 Docker 依赖：

```bash
docker compose up -d mysql redis mailhog
cd backend
mvn spring-boot:run
```

## 一键部署（交互式）

```bash
chmod +x setup.sh
./setup.sh
```

脚本会引导选择邮件模式并生成 `.env`。

## 运行说明

- 前端生产镜像由 `frontend/Dockerfile` 构建，Nginx 反向代理 `/api` 到后端
- 后端生产镜像由 `backend/Dockerfile` 构建
- 数据库迁移统一通过 Flyway 管理（`db/migration`）
- 日志按天滚动，保留 30 天
- 前端保留演示模式兜底（后端不可用时自动降级）