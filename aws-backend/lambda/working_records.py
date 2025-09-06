import json
import boto3
import uuid
from datetime import datetime

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
                'userId': body.get('userId', 'anonymous'),
                'recordId': record_id,
                'date': body.get('date', datetime.now().strftime('%Y-%m-%d')),
                'type': body.get('drinkType', 'BEER'),
                'unit': body.get('unit', 'CAN'),
                'quantity': body.get('count', 1),
                'totalVolumeMl': body.get('volumeMl', 355),
                'abv': body.get('abv'),
                'note': body.get('note', ''),
                'createdAt': datetime.now().isoformat(),
                'updatedAt': datetime.now().isoformat()
            }
            
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
                        'userId': item['userId'],
                        'date': item['date'],
                        'type': item['type'],
                        'unit': item['unit'],
                        'quantity': item['quantity'],
                        'totalVolumeMl': item['totalVolumeMl'],
                        'abv': item['abv'],
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
                KeyConditionExpression='userId = :userId',
                ExpressionAttributeValues={':userId': user_id}
            )
            
            records = []
            for item in response.get('Items', []):
                records.append({
                    'id': item['recordId'],
                    'userId': item['userId'],
                    'date': item['date'],
                    'type': item['type'],
                    'unit': item.get('unit', 'CAN'),
                    'quantity': item['quantity'],
                    'totalVolumeMl': item['totalVolumeMl'],
                    'abv': item.get('abv'),
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
                })
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
