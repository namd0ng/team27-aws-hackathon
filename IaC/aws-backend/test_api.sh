#!/bin/bash

# API 테스트 스크립트

API_URL="https://your-api-gateway-url.execute-api.us-east-1.amazonaws.com/prod"

echo "🧪 회원가입 API 테스트..."
curl -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "testpassword123",
    "name": "테스트 사용자"
  }' | jq .

echo ""
echo "🔐 로그인 API 테스트..."
curl -X POST "$API_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "testpassword123"
  }' | jq .
