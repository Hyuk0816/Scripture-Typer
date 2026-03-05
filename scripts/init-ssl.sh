#!/bin/bash
# Let's Encrypt 인증서 발급 스크립트
# 사용법: ./scripts/init-ssl.sh
set -e

cd "$(dirname "$0")/.."

# .env에서 변수 로드
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

DOMAIN=${DOMAIN:?"Error: DOMAIN not set in .env"}
EMAIL=${CERTBOT_EMAIL:-""}

echo "==> Requesting Let's Encrypt certificate for ${DOMAIN}..."

# 기존 자체 서명 인증서 삭제
docker compose run --rm certbot sh -c \
  "rm -rf /etc/letsencrypt/live/${DOMAIN} /etc/letsencrypt/renewal/${DOMAIN}.conf"

# 실제 인증서 발급
docker compose run --rm certbot certonly --webroot \
  -w /var/www/certbot \
  -d "${DOMAIN}" \
  ${EMAIL:+--email "${EMAIL}"} \
  ${EMAIL:---register-unsafely-without-email} \
  --agree-tos \
  --no-eff-email \
  --force-renewal

# nginx 리로드
docker compose exec nginx nginx -s reload

echo "==> SSL certificate installed for ${DOMAIN}"
