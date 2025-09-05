const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

const TABLE_NAME = 'AlcoLookProfiles';

exports.handler = async (event) => {
    console.log('Event:', JSON.stringify(event, null, 2));
    
    const { httpMethod, pathParameters, body } = event;
    
    try {
        switch (httpMethod) {
            case 'GET':
                return await getProfile(pathParameters?.userId || 'user123');
            case 'POST':
                return await createProfile(JSON.parse(body || '{}'));
            case 'PUT':
                return await updateProfile(pathParameters?.userId || 'user123', JSON.parse(body || '{}'));
            case 'DELETE':
                return await deleteProfile(pathParameters?.userId || 'user123');
            default:
                return response(405, { error: 'Method not allowed' });
        }
    } catch (error) {
        console.error('Error:', error);
        return response(500, { error: error.message });
    }
};

async function getProfile(userId) {
    const params = {
        TableName: TABLE_NAME,
        Key: { userId }
    };
    
    const result = await dynamodb.get(params).promise();
    return response(200, result.Item || {});
}

async function createProfile(profileData) {
    const { userId = 'user123', sex, isSenior65, weeklyGoalStdDrinks } = profileData;
    
    const params = {
        TableName: TABLE_NAME,
        Item: {
            userId,
            sex: sex || 'UNSET',
            isSenior65: isSenior65 || false,
            weeklyGoalStdDrinks: weeklyGoalStdDrinks || null,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        }
    };
    
    await dynamodb.put(params).promise();
    return response(201, params.Item);
}

async function updateProfile(userId, updateData) {
    const { sex, isSenior65, weeklyGoalStdDrinks } = updateData;
    
    const params = {
        TableName: TABLE_NAME,
        Key: { userId },
        UpdateExpression: 'SET #sex = :sex, #isSenior65 = :isSenior65, #weeklyGoalStdDrinks = :weeklyGoalStdDrinks, #updatedAt = :updatedAt',
        ExpressionAttributeNames: {
            '#sex': 'sex',
            '#isSenior65': 'isSenior65',
            '#weeklyGoalStdDrinks': 'weeklyGoalStdDrinks',
            '#updatedAt': 'updatedAt'
        },
        ExpressionAttributeValues: {
            ':sex': sex,
            ':isSenior65': isSenior65,
            ':weeklyGoalStdDrinks': weeklyGoalStdDrinks,
            ':updatedAt': new Date().toISOString()
        },
        ReturnValues: 'ALL_NEW'
    };
    
    const result = await dynamodb.update(params).promise();
    return response(200, result.Attributes);
}

async function deleteProfile(userId) {
    const params = {
        TableName: TABLE_NAME,
        Key: { userId }
    };
    
    await dynamodb.delete(params).promise();
    return response(200, { message: 'Profile deleted successfully' });
}

function response(statusCode, body) {
    return {
        statusCode,
        headers: {
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        },
        body: JSON.stringify(body)
    };
}
