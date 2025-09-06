#!/bin/bash

API_URL="https://iql82o9kv2.execute-api.us-east-1.amazonaws.com/prod"

echo "Testing DynamoDB Drink Records API..."

# Test POST - Create Record
echo "1. Testing POST /drink-records"
curl -X POST "${API_URL}/drink-records" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-123",
    "date": "2024-01-15",
    "drinkType": "BEER",
    "count": 1,
    "volumeMl": 355,
    "abv": 4.5,
    "note": "Test record from curl"
  }' \
  -v

echo -e "\n\n2. Testing GET /drink-records"
curl -X GET "${API_URL}/drink-records?userId=test-user-123" \
  -H "Content-Type: application/json" \
  -v

echo -e "\n\nAPI Test completed."
