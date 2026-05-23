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

DOMAIN="$(prompt_default "请输入前端访问域名或 IP" "localhost")"
ADMIN_USERNAME="$(prompt_default "请输入管理员用户名" "admin")"
ADMIN_PASSWORD="$(prompt_default "请输入管理员密码" "Admin@123456")"
POSTFIX_DOMAIN="$(prompt_default "请输入 Postfix 发信域名" "$DOMAIN")"

if [[ "$DOMAIN" == "localhost" || "$DOMAIN" == "127.0.0.1" ]]; then
  FRONTEND_BASE_URL="http://$DOMAIN"
else
  FRONTEND_BASE_URL="http://$DOMAIN"
fi

JWT_SECRET="$(generate_secret)"
ADMIN_EMAIL="${ADMIN_USERNAME}@${POSTFIX_DOMAIN}"
SMTP_USER="noreply@${POSTFIX_DOMAIN}"

cat > "$ENV_FILE" <<EOF
DB_NAME=lottery_system
DB_USERNAME=lottery
DB_PASSWORD=lottery123
DB_ROOT_PASSWORD=root123456
MYSQL_PORT=3306
REDIS_PORT=6379
SMTP_PORT=587
SMTP_SSL_PORT=465
SMTP_USER=$SMTP_USER
SMTP_PASS=changeit
SMTP_AUTH=false
SMTP_STARTTLS=false
POSTFIX_DOMAIN=$POSTFIX_DOMAIN
BACKEND_PORT=8080
FRONTEND_PORT=80
FRONTEND_BASE_URL=$FRONTEND_BASE_URL
ALLOWED_ORIGINS=$FRONTEND_BASE_URL,http://localhost:5173,http://127.0.0.1:5173,http://localhost:5174,http://127.0.0.1:5174
JWT_SECRET=$JWT_SECRET
ADMIN_USERNAME=$ADMIN_USERNAME
ADMIN_EMAIL=$ADMIN_EMAIL
ADMIN_PASSWORD=$ADMIN_PASSWORD
LOTTERY_DAILY_LIMIT=10
LOTTERY_COOLDOWN_SECONDS=5
EOF

cd "$ROOT_DIR"
docker compose up -d --build

echo
echo "部署完成。"
echo "前端访问地址: $FRONTEND_BASE_URL"
echo "管理后台地址: $FRONTEND_BASE_URL/admin"
echo "管理员账号: $ADMIN_USERNAME"
echo "管理员邮箱: $ADMIN_EMAIL"
