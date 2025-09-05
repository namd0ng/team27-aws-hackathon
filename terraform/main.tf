terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Data sources
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# IAM Role for Rekognition
resource "aws_iam_role" "rekognition_role" {
  name = "${var.project_name}-rekognition-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "rekognition.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "rekognition_policy" {
  role       = aws_iam_role.rekognition_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonRekognitionFullAccess"
}

# IAM Role for Bedrock
resource "aws_iam_role" "bedrock_role" {
  name = "${var.project_name}-bedrock-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "bedrock.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy" "bedrock_invoke_policy" {
  name = "BedrockInvokePolicy"
  role = aws_iam_role.bedrock_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "bedrock:InvokeModel",
          "bedrock:InvokeModelWithResponseStream"
        ]
        Resource = "*"
      }
    ]
  })
}

# S3 Bucket for temporary image storage
resource "aws_s3_bucket" "image_storage" {
  bucket = "${var.project_name}-images-${var.environment}-${data.aws_caller_identity.current.account_id}"
}

resource "aws_s3_bucket_public_access_block" "image_storage_pab" {
  bucket = aws_s3_bucket.image_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "image_storage_lifecycle" {
  bucket = aws_s3_bucket.image_storage.id

  rule {
    id     = "delete_temp_images"
    status = "Enabled"

    expiration {
      days = 1
    }
  }
}

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "application_logs" {
  name              = "/aws/alcolook/${var.environment}"
  retention_in_days = 30
}
