# Sensor Analytics Infrastructure
resource "aws_s3_bucket" "sensor_analytics" {
  bucket = "${var.project_name}-sensor-analytics-${var.environment}"
}

resource "aws_s3_bucket_public_access_block" "sensor_analytics" {
  bucket = aws_s3_bucket.sensor_analytics.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "sensor_analytics" {
  bucket = aws_s3_bucket.sensor_analytics.id

  rule {
    id     = "delete_after_30_days"
    status = "Enabled"

    expiration {
      days = 30
    }
  }
}

# Lambda for gyroscope analysis
resource "aws_lambda_function" "gyroscope_analysis" {
  filename         = "gyroscope_analysis.zip"
  function_name    = "${var.project_name}-gyroscope-analysis-${var.environment}"
  role            = aws_iam_role.lambda_execution.arn
  handler         = "index.handler"
  runtime         = "python3.9"
  timeout         = 30
  memory_size     = 128

  source_code_hash = data.archive_file.gyroscope_analysis.output_base64sha256
}

# Lambda for heart rate analysis
resource "aws_lambda_function" "heartrate_analysis" {
  filename         = "heartrate_analysis.zip"
  function_name    = "${var.project_name}-heartrate-analysis-${var.environment}"
  role            = aws_iam_role.lambda_execution.arn
  handler         = "index.handler"
  runtime         = "python3.9"
  timeout         = 30
  memory_size     = 128

  source_code_hash = data.archive_file.heartrate_analysis.output_base64sha256
}

# Lambda source code archives
data "archive_file" "gyroscope_analysis" {
  type        = "zip"
  output_path = "gyroscope_analysis.zip"
  
  source {
    content = <<EOF
import json
import boto3
import math

def handler(event, context):
    # Analyze gyroscope stability patterns
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']
        
        # Process anonymized gyroscope data
        s3 = boto3.client('s3')
        obj = s3.get_object(Bucket=bucket, Key=key)
        data = json.loads(obj['Body'].read())
        
        # Calculate stability metrics
        stability_score = calculate_stability(data)
        
        print(f"Stability analysis: {stability_score}")
    
    return {'statusCode': 200}

def calculate_stability(gyro_data):
    movements = gyro_data.get('movements', [])
    if not movements:
        return 0.5
    
    total_magnitude = sum(m.get('magnitude', 0) for m in movements)
    avg_magnitude = total_magnitude / len(movements)
    
    return max(0, min(1, 1 - (avg_magnitude / 5.0)))
EOF
    filename = "index.py"
  }
}

data "archive_file" "heartrate_analysis" {
  type        = "zip"
  output_path = "heartrate_analysis.zip"
  
  source {
    content = <<EOF
import json
import boto3

def handler(event, context):
    # Analyze heart rate variability patterns
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = record['s3']['object']['key']
        
        s3 = boto3.client('s3')
        obj = s3.get_object(Bucket=bucket, Key=key)
        data = json.loads(obj['Body'].read())
        
        # Calculate HRV metrics (anonymized)
        hrv_score = calculate_hrv(data)
        
        print(f"HRV analysis: {hrv_score}")
    
    return {'statusCode': 200}

def calculate_hrv(hr_data):
    bpm = hr_data.get('bpm', 70)
    resting_bpm = hr_data.get('resting_bpm', 70)
    
    if resting_bpm > 0:
        change_percent = abs(bpm - resting_bpm) / resting_bpm
        return min(1.0, change_percent)
    
    return 0.0
EOF
    filename = "index.py"
  }
}

# Lambda execution role
resource "aws_iam_role" "lambda_execution" {
  name = "${var.project_name}-lambda-execution-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_basic" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_role_policy" "lambda_s3_access" {
  name = "s3-access"
  role = aws_iam_role.lambda_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ]
        Resource = "${aws_s3_bucket.sensor_analytics.arn}/*"
      }
    ]
  })
}

# S3 bucket notification
resource "aws_s3_bucket_notification" "sensor_analytics" {
  bucket = aws_s3_bucket.sensor_analytics.id

  lambda_function {
    lambda_function_arn = aws_lambda_function.gyroscope_analysis.arn
    events              = ["s3:ObjectCreated:*"]
    filter_prefix       = "gyroscope/"
  }

  lambda_function {
    lambda_function_arn = aws_lambda_function.heartrate_analysis.arn
    events              = ["s3:ObjectCreated:*"]
    filter_prefix       = "heartrate/"
  }

  depends_on = [
    aws_lambda_permission.s3_invoke_gyroscope,
    aws_lambda_permission.s3_invoke_heartrate
  ]
}

# Lambda permissions for S3
resource "aws_lambda_permission" "s3_invoke_gyroscope" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.gyroscope_analysis.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.sensor_analytics.arn
}

resource "aws_lambda_permission" "s3_invoke_heartrate" {
  statement_id  = "AllowExecutionFromS3Bucket"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.heartrate_analysis.function_name
  principal     = "s3.amazonaws.com"
  source_arn    = aws_s3_bucket.sensor_analytics.arn
}

# CloudWatch Dashboard
resource "aws_cloudwatch_dashboard" "sensor_analytics" {
  dashboard_name = "${var.project_name}-sensor-analytics-${var.environment}"

  dashboard_body = jsonencode({
    widgets = [
      {
        type   = "metric"
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/Lambda", "Invocations", "FunctionName", aws_lambda_function.gyroscope_analysis.function_name],
            ["AWS/Lambda", "Duration", "FunctionName", aws_lambda_function.gyroscope_analysis.function_name],
            ["AWS/Lambda", "Errors", "FunctionName", aws_lambda_function.gyroscope_analysis.function_name]
          ]
          period = 300
          stat   = "Sum"
          region = var.aws_region
          title  = "Gyroscope Analysis Metrics"
        }
      },
      {
        type   = "metric"
        width  = 12
        height = 6
        properties = {
          metrics = [
            ["AWS/S3", "NumberOfObjects", "BucketName", aws_s3_bucket.sensor_analytics.bucket],
            ["AWS/S3", "BucketSizeBytes", "BucketName", aws_s3_bucket.sensor_analytics.bucket]
          ]
          period = 86400
          stat   = "Average"
          region = var.aws_region
          title  = "Sensor Data Storage"
        }
      }
    ]
  })
}
