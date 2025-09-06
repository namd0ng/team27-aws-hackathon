import json
import boto3
import jwt
import os
from datetime import datetime

def lambda_handler(event, context):
    # CORS 헤더
    headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
        'Access-Control-Allow-Methods': 'PUT, OPTIONS'
    }
    
    # OPTIONS 요청 처리 (CORS preflight)
    if event['httpMethod'] == 'OPTIONS':
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({'message': 'CORS preflight'})
        }
    
    try:
        # JWT 토큰 검증
        auth_header = event.get('headers', {}).get('Authorization') or event.get('headers', {}).get('authorization')
        print(f"Authorization header: {auth_header}")
        
        if not auth_header or not auth_header.startswith('Bearer '):
            return {
                'statusCode': 401,
                'headers': headers,
                'body': json.dumps({'error': 'Authorization token required'})
            }
        
        token = auth_header.replace('Bearer ', '')
        jwt_secret = os.environ.get('JWT_SECRET', 'your-super-secret-jwt-key-change-this-in-production')
        
        try:
            # 무제한 토큰이므로 exp 검증 비활성화
            decoded_token = jwt.decode(token, jwt_secret, algorithms=['HS256'], options={"verify_exp": False})
            token_user_id = decoded_token.get('user_id')
            print(f"Decoded token user_id: {token_user_id}")
        except jwt.InvalidTokenError as e:
            print(f"JWT decode error: {e}")
            return {
                'statusCode': 401,
                'headers': headers,
                'body': json.dumps({'error': 'Invalid token'})
            }
        
        # DynamoDB 클라이언트 초기화
        dynamodb = boto3.resource('dynamodb')
        table = dynamodb.Table('alcolook-user-profiles')
        
        # 요청 본문 파싱
        body = json.loads(event['body'])
        user_id = body.get('userId') or body.get('user_id')
        sex = body.get('sex', 'UNSET')
        age = body.get('age')
        is_senior_65 = body.get('isSenior65', False)
        weekly_goal = body.get('weeklyGoalStdDrinks')
        
        print(f"Request: user_id={user_id}, sex={sex}, age={age}")
        
        # 토큰의 사용자 ID와 요청의 사용자 ID 일치 확인
        if token_user_id != user_id:
            print(f"User ID mismatch: token={token_user_id}, request={user_id}")
            return {
                'statusCode': 403,
                'headers': headers,
                'body': json.dumps({'error': 'Access denied'})
            }
        
        # 필수 필드 검증
        if not user_id:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({'error': 'userId is required'})
            }
        
        # 업데이트할 데이터 준비
        update_expression = "SET sex = :sex, updated_at = :time"
        expression_values = {
            ':sex': sex,
            ':time': datetime.utcnow().isoformat() + 'Z'
        }
        
        # 선택적 필드들 추가
        if age is not None:
            update_expression += ", age = :age"
            expression_values[':age'] = age
            
        if is_senior_65 is not None:
            update_expression += ", isSenior65 = :senior"
            expression_values[':senior'] = is_senior_65
            
        if weekly_goal is not None:
            update_expression += ", weeklyGoalStdDrinks = :goal"
            expression_values[':goal'] = weekly_goal
        
        print(f"DynamoDB update: {update_expression}")
        
        # DynamoDB 업데이트 (upsert)
        response = table.update_item(
            Key={'user_id': user_id},
            UpdateExpression=update_expression,
            ExpressionAttributeValues=expression_values,
            ReturnValues='ALL_NEW'
        )
        
        # 응답 데이터 준비
        updated_item = response['Attributes']
        print(f"Profile updated successfully for user: {user_id}")
        
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({
                'message': 'Profile updated successfully',
                'profile': {
                    'user_id': updated_item['user_id'],
                    'sex': updated_item.get('sex', 'UNSET'),
                    'age': int(updated_item['age']) if updated_item.get('age') else None,
                    'isSenior65': updated_item.get('isSenior65', False),
                    'weeklyGoalStdDrinks': int(updated_item.get('weeklyGoalStdDrinks', 0)) if updated_item.get('weeklyGoalStdDrinks') else None,
                    'updated_at': updated_item.get('updated_at')
                }
            })
        }
        
    except Exception as e:
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': headers,
            'body': json.dumps({'error': f'Internal server error: {str(e)}'})
        }
