import json
import boto3
import hashlib
import uuid
from datetime import datetime

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('alcolook-users')

def lambda_handler(event, context):
    try:
        # CORS 헤더
        headers = {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        }
        
        # OPTIONS 요청 처리
        if event['httpMethod'] == 'OPTIONS':
            return {
                'statusCode': 200,
                'headers': headers,
                'body': ''
            }
        
        # 요청 본문 파싱
        body = json.loads(event['body'])
        email = body.get('email')
        password = body.get('password')
        name = body.get('name')
        
        # 입력 검증
        if not email or not password or not name:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({
                    'error': '이메일, 비밀번호, 이름은 필수입니다.'
                })
            }
        
        # 이메일 중복 확인
        try:
            response = table.get_item(Key={'email': email})
            if 'Item' in response:
                return {
                    'statusCode': 409,
                    'headers': headers,
                    'body': json.dumps({
                        'error': '이미 존재하는 이메일입니다.'
                    })
                }
        except Exception as e:
            print(f"Error checking email: {e}")
        
        # 비밀번호 해시화
        password_hash = hashlib.sha256(password.encode()).hexdigest()
        
        # 사용자 ID 생성
        user_id = str(uuid.uuid4())
        
        # 사용자 정보 저장
        user_item = {
            'email': email,
            'user_id': user_id,
            'name': name,
            'password_hash': password_hash,
            'created_at': datetime.utcnow().isoformat(),
            'updated_at': datetime.utcnow().isoformat()
        }
        
        table.put_item(Item=user_item)
        
        return {
            'statusCode': 201,
            'headers': headers,
            'body': json.dumps({
                'message': '회원가입이 완료되었습니다.',
                'user_id': user_id,
                'email': email,
                'name': name
            })
        }
        
    except Exception as e:
        print(f"Error: {e}")
        return {
            'statusCode': 500,
            'headers': headers,
            'body': json.dumps({
                'error': '서버 오류가 발생했습니다.'
            })
        }
