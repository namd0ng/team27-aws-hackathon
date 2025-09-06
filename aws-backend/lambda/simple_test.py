import json

def lambda_handler(event, context):
    return {
        'statusCode': 200,
        'body': json.dumps({
            'success': True,
            'message': 'Lambda is working!',
            'event': str(event)
        })
    }
