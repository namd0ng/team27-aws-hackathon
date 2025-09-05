import json
import boto3
import uuid
from datetime import datetime, timedelta
from botocore.exceptions import ClientError

dynamodb = boto3.resource('dynamodb')
users_table = dynamodb.Table('alcolook-users')

# SES는 선택적으로 사용 (설정되지 않으면 로그만 출력)
try:
    ses = boto3.client('ses')
    SES_AVAILABLE = True
except Exception:
    SES_AVAILABLE = False

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
                'body': json.dumps({'message': 'OK'})
            }
        
        # 요청 본문 파싱
        try:
            body = json.loads(event['body'])
            email = body.get('email', '').strip().lower()
        except (json.JSONDecodeError, TypeError):
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({
                    'error': '잘못된 요청 형식입니다.'
                })
            }
        
        # 입력 검증
        if not email or '@' not in email:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({
                    'error': '유효한 이메일을 입력해주세요.'
                })
            }
        
        # 사용자 존재 확인
        try:
            response = users_table.get_item(Key={'email': email})
            if 'Item' not in response:
                # 보안상 사용자가 존재하지 않아도 성공 메시지 반환
                print(f"Password reset requested for non-existent email: {email}")
                return {
                    'statusCode': 200,
                    'headers': headers,
                    'body': json.dumps({
                        'message': '비밀번호 재설정 링크를 이메일로 보내드렸습니다.'
                    })
                }
            
            user = response['Item']
            print(f"Password reset requested for user: {email}")
            
        except ClientError as e:
            print(f"DynamoDB error getting user: {e}")
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
        
        # 재설정 정보를 users 테이블에 저장 (별도 테이블 대신)
        try:
            users_table.update_item(
                Key={'email': email},
                UpdateExpression='SET reset_token = :token, reset_expires = :expires, reset_created = :created',
                ExpressionAttributeValues={
                    ':token': reset_token,
                    ':expires': expires_at.isoformat(),
                    ':created': datetime.utcnow().isoformat()
                }
            )
            print(f"Reset token saved for user: {email}")
        except ClientError as e:
            print(f"Error saving reset token: {e}")
            return {
                'statusCode': 500,
                'headers': headers,
                'body': json.dumps({
                    'error': '서버 오류가 발생했습니다.'
                })
            }
        
        # 이메일 발송 (SES가 사용 가능한 경우에만)
        if SES_AVAILABLE:
            reset_url = f"https://alcolook-app.com/reset-password?token={reset_token}"
            
            email_body = f"""안녕하세요, {user.get('name', '사용자')}님!

AlcoLook 비밀번호 재설정을 요청하셨습니다.

아래 링크를 클릭하여 새 비밀번호를 설정해주세요:
{reset_url}

이 링크는 1시간 후에 만료됩니다.

만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.

감사합니다.
AlcoLook 팀"""
            
            try:
                # 검증된 이메일 주소 사용 (실제 운영 시 변경 필요)
                ses.send_email(
                    Source='noreply@alcolook.com',  # 실제 검증된 도메인으로 변경 필요
                    Destination={'ToAddresses': [email]},
                    Message={
                        'Subject': {'Data': 'AlcoLook 비밀번호 재설정'},
                        'Body': {'Text': {'Data': email_body}}
                    }
                )
                print(f"Password reset email sent to {email}")
            except ClientError as e:
                print(f"SES error sending email: {e}")
                # 이메일 발송 실패해도 사용자에게는 성공 메시지 반환
        else:
            print(f"SES not available. Reset token generated for {email}: {reset_token}")
        
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({
                'message': '비밀번호 재설정 링크를 이메일로 보내드렸습니다.'
            })
        }
        
    except Exception as e:
        print(f"Unexpected error: {e}")
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
