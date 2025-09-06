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

# IAM Role for Rekognition (Real-time analysis only)
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
  policy_arn = "arn:aws:iam::aws:policy/AmazonRekognitionReadOnlyAccess"
}

# IAM Role for Bedrock (AI Analysis)
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

# CloudWatch Log Group (Anonymous analytics only)
resource "aws_cloudwatch_log_group" "application_logs" {
  name              = "/aws/alcolook/${var.environment}"
  retention_in_days = 30
}
