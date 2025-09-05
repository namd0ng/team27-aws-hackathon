import json
import boto3
import hashlib
import jwt
import os
from datetime import datetime, timedelta

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('alcolook-users')

# JWT 시크릿 키 (환경변수에서 가져오기)
JWT_SECRET = os.environ.get('JWT_SECRET', 'your-super-secret-jwt-key-change-this-in-production')

def lambda_handler(event, context):
    try:
        # CORS 헤더
        headers = {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        }
        
        # OPTIONS 요청 처리
        if event['httpMethod'] == 'OPTIONS':
            return {
                'statusCode': 200,
                'headers': headers,
                'body': json.dumps({'message': 'OK'})
            }
        
        # 요청 본문 파싱
        body = json.loads(event['body'])
        email = body.get('email')
        password = body.get('password')
        
        # 입력 검증
        if not email or not password:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({
                    'error': '이메일과 비밀번호를 입력해주세요.'
                })
            }
        
        # 사용자 조회
        response = table.get_item(Key={'email': email})
        
        if 'Item' not in response:
            return {
                'statusCode': 401,
                'headers': headers,
                'body': json.dumps({
                    'error': '이메일 또는 비밀번호가 잘못되었습니다.'
                })
            }
        
        user = response['Item']
        
        # 비밀번호 검증
        password_hash = hashlib.sha256(password.encode()).hexdigest()
        if user['password_hash'] != password_hash:
            return {
                'statusCode': 401,
                'headers': headers,
                'body': json.dumps({
                    'error': '이메일 또는 비밀번호가 잘못되었습니다.'
                })
            }
        
        # JWT 토큰 생성
        payload = {
            'user_id': user['user_id'],
            'email': user['email'],
            'name': user['name'],
            'exp': datetime.utcnow() + timedelta(days=7)  # 7일 후 만료
        }
        token = jwt.encode(payload, JWT_SECRET, algorithm='HS256')
        
        # 로그인 성공
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({
                'message': '로그인이 완료되었습니다.',
                'user_id': user['user_id'],
                'email': user['email'],
                'name': user['name'],
                'token': token
            })
        }
        
    except Exception as e:
        print(f"Error: {e}")
        return {
            'statusCode': 500,
            'headers': {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            'body': json.dumps({
                'error': '서버 오류가 발생했습니다.'
            })
        }
