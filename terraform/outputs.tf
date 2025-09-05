output "rekognition_role_arn" {
  description = "ARN of the Rekognition service role"
  value       = aws_iam_role.rekognition_role.arn
}

output "bedrock_role_arn" {
  description = "ARN of the Bedrock service role"
  value       = aws_iam_role.bedrock_role.arn
}

output "image_bucket_name" {
  description = "Name of the S3 bucket for image storage"
  value       = aws_s3_bucket.image_storage.bucket
}

output "log_group_name" {
  description = "Name of the CloudWatch log group"
  value       = aws_cloudwatch_log_group.application_logs.name
}

output "aws_region" {
  description = "AWS region"
  value       = data.aws_region.current.name
}

output "account_id" {
  description = "AWS Account ID"
  value       = data.aws_caller_identity.current.account_id
}
