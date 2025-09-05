# AlcoLook AWS Backend

AlcoLook 앱의 사용자 인증을 위한 AWS 서버리스 백엔드입니다.

## 아키텍처

- **API Gateway**: REST API 엔드포인트
- **Lambda Functions**: 비즈니스 로직 (Python 3.9)
- **DynamoDB**: 사용자 데이터 저장
- **SES**: 이메일 발송 (비밀번호 재설정)

## 배포 방법

### 1. 사전 요구사항

- AWS CLI 설치 및 구성
- Python 3.9+
- 적절한 AWS 권한 (CloudFormation, Lambda, DynamoDB, API Gateway, SES)

### 2. AWS CLI 구성

```bash
aws configure
```

### 3. 배포 실행

```bash
cd aws-backend
./deploy.sh
```

### 4. SES 설정 (선택사항)

비밀번호 재설정 이메일 발송을 위해 SES 설정이 필요합니다:

1. AWS Console에서 SES 서비스로 이동
2. 발송자 이메일 주소 인증
3. `forgot_password.py`에서 `Source` 이메일 주소 수정

## API 엔드포인트

### 회원가입
```
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "사용자명"
}
```

### 로그인
```
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### 비밀번호 찾기
```
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

## 데이터베이스 스키마

### Users Table (alcolook-users)
- `email` (String, Primary Key): 사용자 이메일
- `user_id` (String): 고유 사용자 ID
- `name` (String): 사용자 이름
- `password_hash` (String): 해시된 비밀번호
- `created_at` (String): 생성 시간
- `updated_at` (String): 수정 시간
- `last_login` (String): 마지막 로그인 시간

### Password Resets Table (alcolook-password-resets)
- `email` (String, Partition Key): 사용자 이메일
- `reset_token` (String, Sort Key): 재설정 토큰
- `expires_at` (String, TTL): 만료 시간
- `created_at` (String): 생성 시간
- `used` (Boolean): 사용 여부

## 보안 고려사항

1. **JWT Secret**: 프로덕션에서는 AWS Secrets Manager 사용 권장
2. **HTTPS**: API Gateway에서 자동으로 HTTPS 제공
3. **CORS**: 필요에 따라 CORS 설정 조정
4. **Rate Limiting**: API Gateway에서 throttling 설정 가능

## 비용 최적화

- DynamoDB: Pay-per-request 모드 사용
- Lambda: 사용량 기반 과금
- API Gateway: 요청 수 기반 과금

## 모니터링

- CloudWatch Logs: Lambda 함수 로그
- CloudWatch Metrics: API Gateway 및 Lambda 메트릭
- X-Ray: 분산 추적 (필요시 활성화)

## 문제 해결

### 배포 실패
1. AWS CLI 권한 확인
2. CloudFormation 스택 상태 확인
3. Lambda 함수 로그 확인

### API 호출 실패
1. CORS 설정 확인
2. API Gateway 배포 상태 확인
3. Lambda 함수 실행 권한 확인
