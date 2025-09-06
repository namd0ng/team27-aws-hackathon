import json
import boto3
import uuid
from datetime import datetime
from decimal import Decimal

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('alcolook-drink-records')

def lambda_handler(event, context):
    print(f"Event: {json.dumps(event)}")
    
    try:
        method = event['httpMethod']
        
        if method == 'POST':
            return create_record(event)
        elif method == 'GET':
            return get_records(event)
        elif method == 'PUT':
            return update_record(event)
        elif method == 'DELETE':
            return delete_record(event)
        else:
            return {
                'statusCode': 405,
                'headers': {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Headers': 'Content-Type',
                    'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
                },
                'body': json.dumps({'success': False, 'message': 'Method not allowed'})
            }
    except Exception as e:
        print(f"Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': False, 'message': str(e)})
        }

def create_record(event):
    try:
        body = json.loads(event['body'])
        print(f"Create record body: {body}")
        
        record_id = str(uuid.uuid4())
        
        record = {
            'userId': body['userId'],
            'recordId': record_id,
            'date': body['date'],
            'type': body['drinkType'],
            'unit': body.get('unit', 'CAN'),
            'quantity': body['count'],
            'totalVolumeMl': body.get('volumeMl', 0),
            'abv': Decimal(str(body.get('abv', 0))) if body.get('abv') else None,
            'note': body.get('note', ''),
            'createdAt': datetime.utcnow().isoformat(),
            'updatedAt': datetime.utcnow().isoformat()
        }
        
        print(f"Putting item: {record}")
        table.put_item(Item=record)
        
        response_record = {
            'id': record_id,
            'userId': record['userId'],
            'date': record['date'],
            'type': record['type'],
            'unit': record['unit'],
            'quantity': record['quantity'],
            'totalVolumeMl': record['totalVolumeMl'],
            'abv': float(record['abv']) if record['abv'] else None,
            'note': record['note'],
            'createdAt': record['createdAt'],
            'updatedAt': record['updatedAt']
        }
        
        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({
                'success': True,
                'message': 'Record created successfully',
                'data': response_record
            })
        }
    except Exception as e:
        print(f"Create record error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': False, 'message': str(e)})
        }

def get_records(event):
    try:
        user_id = event['queryStringParameters']['userId']
        print(f"Getting records for user: {user_id}")
        
        response = table.query(
            KeyConditionExpression='userId = :userId',
            ExpressionAttributeValues={':userId': user_id}
        )
        
        records = []
        for item in response['Items']:
            record = {
                'id': item['recordId'],
                'userId': item['userId'],
                'date': item['date'],
                'type': item['type'],
                'unit': item.get('unit', 'CAN'),
                'quantity': item['quantity'],
                'totalVolumeMl': item['totalVolumeMl'],
                'abv': float(item['abv']) if item.get('abv') else None,
                'note': item.get('note', ''),
                'createdAt': item['createdAt'],
                'updatedAt': item['updatedAt']
            }
            records.append(record)
        
        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({
                'success': True,
                'message': 'Records retrieved successfully',
                'data': records
            })
        }
    except Exception as e:
        print(f"Get records error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': False, 'message': str(e)})
        }

def update_record(event):
    try:
        body = json.loads(event['body'])
        user_id = body['userId']
        record_id = body['recordId']
        
        table.update_item(
            Key={'userId': user_id, 'recordId': record_id},
            UpdateExpression='SET #type = :type, quantity = :quantity, totalVolumeMl = :volumeMl, abv = :abv, note = :note, updatedAt = :updatedAt',
            ExpressionAttributeNames={'#type': 'type'},
            ExpressionAttributeValues={
                ':type': body['drinkType'],
                ':quantity': body['count'],
                ':volumeMl': body.get('volumeMl', 0),
                ':abv': Decimal(str(body.get('abv', 0))) if body.get('abv') else None,
                ':note': body.get('note', ''),
                ':updatedAt': datetime.utcnow().isoformat()
            }
        )
        
        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': True, 'message': 'Record updated successfully'})
        }
    except Exception as e:
        print(f"Update record error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': False, 'message': str(e)})
        }

def delete_record(event):
    try:
        user_id = event['queryStringParameters']['userId']
        record_id = event['queryStringParameters']['recordId']
        
        table.delete_item(
            Key={'userId': user_id, 'recordId': record_id}
        )
        
        return {
            'statusCode': 200,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': True, 'message': 'Record deleted successfully'})
        }
    except Exception as e:
        print(f"Delete record error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
            },
            'body': json.dumps({'success': False, 'message': str(e)})
        }
