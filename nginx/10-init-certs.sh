#!/bin/sh
# nginx 시작 전 인증서가 없으면 자체 서명 인증서 생성 (초기 부팅용)
set -e

CERT_DIR="/etc/letsencrypt/live/${DOMAIN}"

if [ ! -f "${CERT_DIR}/fullchain.pem" ]; then
  echo "No SSL certificate found. Creating self-signed certificate for initial startup..."
  mkdir -p "${CERT_DIR}"
  openssl req -x509 -nodes -newkey rsa:2048 -days 1 \
    -keyout "${CERT_DIR}/privkey.pem" \
    -out "${CERT_DIR}/fullchain.pem" \
    -subj "/CN=localhost"
  echo "Self-signed certificate created. Run init-ssl.sh to get a real certificate."
fi
