import json
import boto3
from datetime import datetime
from decimal import Decimal

def lambda_handler(event, context):
    # CORS 헤더
    headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type',
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
        # DynamoDB 클라이언트 초기화
        dynamodb = boto3.resource('dynamodb')
        table = dynamodb.Table('alcolook-user-profiles')
        
        # 요청 본문 파싱
        body = json.loads(event['body'])
        user_id = body.get('user_id')
        gender = body.get('gender', 'UNSET')
        age = body.get('age', 0)
        is_senior_65 = body.get('isSenior65', False)
        weekly_goal = body.get('weeklyGoalStdDrinks')
        
        # 필수 필드 검증
        if not user_id:
            return {
                'statusCode': 400,
                'headers': headers,
                'body': json.dumps({'error': 'user_id is required'})
            }
        
        # 업데이트할 데이터 준비
        update_expression = "SET gender = :gender, age = :age, isSenior65 = :senior, updated_at = :time"
        expression_values = {
            ':gender': gender,
            ':age': age,
            ':senior': is_senior_65,
            ':time': datetime.utcnow().isoformat() + 'Z'
        }
        
        # weeklyGoalStdDrinks가 있으면 추가
        if weekly_goal is not None:
            update_expression += ", weeklyGoalStdDrinks = :goal"
            expression_values[':goal'] = weekly_goal
        
        # DynamoDB 업데이트
        response = table.update_item(
            Key={'user_id': user_id},
            UpdateExpression=update_expression,
            ExpressionAttributeValues=expression_values,
            ReturnValues='ALL_NEW'
        )
        
        # 응답 데이터 준비
        updated_item = response['Attributes']
        
        return {
            'statusCode': 200,
            'headers': headers,
            'body': json.dumps({
                'message': 'Profile updated successfully',
                'profile': {
                    'user_id': updated_item['user_id'],
                    'gender': updated_item['gender'],
                    'age': int(updated_item['age']),
                    'isSenior65': updated_item['isSenior65'],
                    'weeklyGoalStdDrinks': int(updated_item.get('weeklyGoalStdDrinks', 0)) if updated_item.get('weeklyGoalStdDrinks') else None,
                    'updated_at': updated_item['updated_at']
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
