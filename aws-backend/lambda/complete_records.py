import json
import boto3
import uuid
from datetime import datetime
from decimal import Decimal

class DecimalEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        return super(DecimalEncoder, self).default(obj)

def lambda_handler(event, context):
    try:
        # DynamoDB 연결
        dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
        table = dynamodb.Table('alcolook-drink-records')
        
        method = event.get('httpMethod', 'POST')
        
        if method == 'POST':
            # 기록 생성
            body = json.loads(event.get('body', '{}'))
            
            record_id = str(uuid.uuid4())
            
            item = {
                'user_id': body.get('userId', 'anonymous'),
                'record_id': record_id,
                'date': body.get('date', datetime.now().strftime('%Y-%m-%d')),
                'type': body.get('drinkType', 'BEER'),
                'unit': body.get('unit', 'CAN'),
                'quantity': body.get('count', 1),
                'totalVolumeMl': body.get('volumeMl', 355),
                'note': body.get('note', ''),
                'createdAt': datetime.now().isoformat(),
                'updatedAt': datetime.now().isoformat()
            }
            
            # abv를 Decimal로 변환
            if body.get('abv') is not None:
                item['abv'] = Decimal(str(body.get('abv')))
            
            # DynamoDB에 저장
            table.put_item(Item=item)
            
            return {
                'statusCode': 200,
                'headers': {
                    'Access-Control-Allow-Origin': '*',
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'success': True,
                    'message': 'Record created successfully',
                    'data': {
                        'id': record_id,
                        'userId': item['user_id'],
                        'date': item['date'],
                        'type': item['type'],
                        'unit': item['unit'],
                        'quantity': item['quantity'],
                        'totalVolumeMl': item['totalVolumeMl'],
                        'abv': float(item['abv']) if item.get('abv') else None,
                        'note': item['note'],
                        'createdAt': item['createdAt'],
                        'updatedAt': item['updatedAt']
                    }
                })
            }
            
        elif method == 'GET':
            # 기록 조회
            user_id = event.get('queryStringParameters', {}).get('userId', 'anonymous')
            
            response = table.query(
                KeyConditionExpression='user_id = :userId',
                ExpressionAttributeValues={':userId': user_id}
            )
            
            records = []
            for item in response.get('Items', []):
                records.append({
                    'id': item['record_id'],
                    'userId': item['user_id'],
                    'date': item['date'],
                    'type': item['type'],
                    'unit': item.get('unit', 'CAN'),
                    'quantity': item['quantity'],
                    'totalVolumeMl': item['totalVolumeMl'],
                    'abv': float(item['abv']) if item.get('abv') else None,
                    'note': item.get('note', ''),
                    'createdAt': item['createdAt'],
                    'updatedAt': item['updatedAt']
                })
            
            return {
                'statusCode': 200,
                'headers': {
                    'Access-Control-Allow-Origin': '*',
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'success': True,
                    'message': 'Records retrieved successfully',
                    'data': records
                }, cls=DecimalEncoder)
            }
        
        else:
            return {
                'statusCode': 405,
                'headers': {
                    'Access-Control-Allow-Origin': '*',
                    'Content-Type': 'application/json'
                },
                'body': json.dumps({
                    'success': False,
                    'message': 'Method not allowed'
                })
            }
            
    except Exception as e:
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Content-Type': 'application/json'
            },
            'body': json.dumps({
                'success': False,
                'message': f'Error: {str(e)}'
            })
        }
