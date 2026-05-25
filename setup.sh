#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$ROOT_DIR/.env"

prompt_default() {
  local prompt="$1"
  local default_value="$2"
  local input
  read -r -p "$prompt [$default_value]: " input
  if [[ -z "$input" ]]; then
    echo "$default_value"
  else
    echo "$input"
  fi
}

generate_secret() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -hex 32
  else
    python3 - <<'PY'
import secrets
print(secrets.token_hex(32))
PY
  fi
}

echo "============================================"
echo "  抽奖系统 - 一键部署配置"
echo "============================================"
echo ""

DOMAIN="$(prompt_default "请输入前端访问域名或 IP" "localhost")"
ADMIN_USERNAME="$(prompt_default "请输入管理员用户名" "admin")"
ADMIN_PASSWORD="$(prompt_default "请输入管理员密码" "Admin@123456")"

echo ""
echo "--- 邮件发送配置 ---"
echo "1) MailHog（默认，邮件不真发，在 http://localhost:8025 查看，推荐测试用）"
echo "2) 外部 SMTP Relay（QQ/163/SendGrid 等，需要真实 SMTP 账号）"
echo "3) 内置 postfix 容器（需要 25 端口）"
MAIL_MODE="$(prompt_default "请选择邮件模式 [1/2/3]" "1")"

case "$MAIL_MODE" in
  1)
    SMTP_HOST="mailhog"
    SMTP_PORT="1025"
    SMTP_USER="noreply@lottery.local"
    SMTP_PASS=""
    SMTP_AUTH="false"
    SMTP_STARTTLS="false"
    POSTFIX_DOMAIN="example.com"
    ;;
  2)
    SMTP_HOST="$(prompt_default "SMTP 服务器地址" "smtp.qq.com")"
    SMTP_PORT="$(prompt_default "SMTP 端口" "587")"
    SMTP_USER="$(prompt_default "SMTP 发信账号" "your-email@qq.com")"
    SMTP_PASS="$(prompt_default "SMTP 密码/授权码" "")"
    SMTP_AUTH="true"
    SMTP_STARTTLS="true"
    POSTFIX_DOMAIN="example.com"
    ;;
  3)
    SMTP_HOST="postfix"
    SMTP_PORT="587"
    SMTP_USER="noreply@example.com"
    SMTP_PASS="changeit"
    SMTP_AUTH="false"
    SMTP_STARTTLS="false"
    POSTFIX_DOMAIN="$(prompt_default "请输入发信域名" "$DOMAIN")"
    ;;
esac

if [[ "$DOMAIN" == "localhost" || "$DOMAIN" == "127.0.0.1" ]]; then
  FRONTEND_BASE_URL="http://$DOMAIN"
else
  FRONTEND_BASE_URL="http://$DOMAIN"
fi

JWT_SECRET="$(generate_secret)"
ADMIN_EMAIL="${ADMIN_USERNAME}@${POSTFIX_DOMAIN}"

cat > "$ENV_FILE" <<EOF
DB_NAME=lottery_system
DB_USERNAME=lottery
DB_PASSWORD=lottery123
DB_ROOT_PASSWORD=root123456
MYSQL_PORT=3306
REDIS_PORT=6379
SMTP_HOST=$SMTP_HOST
SMTP_PORT=$SMTP_PORT
SMTP_USER=$SMTP_USER
SMTP_PASS=$SMTP_PASS
SMTP_AUTH=$SMTP_AUTH
SMTP_STARTTLS=$SMTP_STARTTLS
MAIL_DEBUG=false
MAILHOG_SMTP_PORT=1025
MAILHOG_UI_PORT=8025
POSTFIX_DOMAIN=$POSTFIX_DOMAIN
BACKEND_PORT=8080
FRONTEND_PORT=80
FRONTEND_BASE_URL=$FRONTEND_BASE_URL
ALLOWED_ORIGINS=$FRONTEND_BASE_URL,http://localhost:5173
JWT_SECRET=$JWT_SECRET
ADMIN_USERNAME=$ADMIN_USERNAME
ADMIN_EMAIL=$ADMIN_EMAIL
ADMIN_PASSWORD=$ADMIN_PASSWORD
LOTTERY_DAILY_LIMIT=10
LOTTERY_COOLDOWN_SECONDS=5
EOF

cd "$ROOT_DIR"

if [[ "$MAIL_MODE" == "3" ]]; then
  docker compose --profile mail up -d --build
else
  docker compose up -d --build
fi

echo ""
echo "============================================"
echo "  部署完成！"
echo "============================================"
echo "前端访问地址: $FRONTEND_BASE_URL"
echo "管理后台地址: $FRONTEND_BASE_URL/admin"
echo "管理员账号:   $ADMIN_USERNAME"
echo "管理员邮箱:   $ADMIN_EMAIL"
echo ""
if [[ "$MAIL_MODE" == "1" ]]; then
  echo "邮件模式:     MailHog（邮件不真发）"
  echo "邮件查看:     http://localhost:8025"
elif [[ "$MAIL_MODE" == "2" ]]; then
  echo "邮件模式:     外部 SMTP Relay ($SMTP_HOST:$SMTP_PORT)"
  echo "SMTP 账号:    $SMTP_USER"
else
  echo "邮件模式:     内置 postfix (需要 25 端口)"
fi