import json

def lambda_handler(event, context):
    print(f"Event received: {json.dumps(event)}")
    
    return {
        'statusCode': 200,
        'headers': {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Headers': 'Content-Type',
            'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE'
        },
        'body': json.dumps({
            'success': True,
            'message': 'Test API working',
            'data': {
                'id': 'test-record-123',
                'userId': 'test-user',
                'date': '2024-01-15',
                'type': 'BEER',
                'unit': 'CAN',
                'quantity': 1,
                'totalVolumeMl': 355,
                'abv': 4.5,
                'note': 'Test record',
                'createdAt': '2024-01-15T10:00:00Z',
                'updatedAt': '2024-01-15T10:00:00Z'
            }
        })
    }
