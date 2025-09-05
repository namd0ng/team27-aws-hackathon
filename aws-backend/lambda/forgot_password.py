import json
import boto3
import uuid
from datetime import datetime, timedelta

dynamodb = boto3.resource('dynamodb')
users_table = dynamodb.Table('alcolook-users')
reset_table = dynamodb.Table('alcolook-password-resets')

ses = boto3.client('ses')

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
        
        # 입력 검증
        if not email:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({
                    'error': '이메일을 입력해주세요.'
                })
            }
        
        # 사용자 존재 확인
        try:
            response = users_table.get_item(Key={'email': email})
            if 'Item' not in response:
                # 보안상 사용자가 존재하지 않아도 성공 메시지 반환
                return {
                    'statusCode': 200,
                    'headers': headers,
                    'body': json.dumps({
                        'message': '비밀번호 재설정 링크를 이메일로 보내드렸습니다.'
                    })
                }
            
            user = response['Item']
        except Exception as e:
            print(f"Error getting user: {e}")
            return {
                'statusCode': 500,
                'headers': headers,
                'body': json.dumps({
                    'error': '서버 오류가 발생했습니다.'
                })
            }
        
        # 재설정 토큰 생성
        reset_token = str(uuid.uuid4())
        expires_at = datetime.utcnow() + timedelta(hours=1)  # 1시간 만료
        
        # 재설정 토큰 저장
        reset_item = {
            'email': email,
            'reset_token': reset_token,
            'expires_at': expires_at.isoformat(),
            'created_at': datetime.utcnow().isoformat(),
            'used': False
        }
        
        reset_table.put_item(Item=reset_item)
        
        # 이메일 발송
        reset_url = f"https://alcolook-app.com/reset-password?token={reset_token}"
        
        email_body = f"""안녕하세요, {user['name']}님!

AlcoLook 비밀번호 재설정을 요청하셨습니다.

아래 링크를 클릭하여 새 비밀번호를 설정해주세요:
{reset_url}

이 링크는 1시간 후에 만료됩니다.

만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.

감사합니다.
AlcoLook 팀"""
        
        try:
            ses.send_email(
                Source='noreply.alcolook@gmail.com',
                Destination={'ToAddresses': [email]},
                Message={
                    'Subject': {'Data': 'AlcoLook 비밀번호 재설정'},
                    'Body': {'Text': {'Data': email_body}}
                }
            )
            print(f"Password reset email sent to {email}")
        except Exception as e:
            print(f"Error sending email: {e}")
            # 이메일 발송 실패해도 사용자에게는 성공 메시지 반환
        
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({
                'message': '비밀번호 재설정 링크를 이메일로 보내드렸습니다.'
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
