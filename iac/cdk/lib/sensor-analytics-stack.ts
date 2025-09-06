import * as cdk from 'aws-cdk-lib';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as s3n from 'aws-cdk-lib/aws-s3-notifications';
import * as cloudwatch from 'aws-cdk-lib/aws-cloudwatch';
import { Construct } from 'constructs';

export interface SensorAnalyticsStackProps extends cdk.StackProps {
  environment: string;
  projectName: string;
}

export class SensorAnalyticsStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: SensorAnalyticsStackProps) {
    super(scope, id, props);

    const { environment, projectName } = props;

    // S3 Bucket for anonymized sensor analytics
    const sensorAnalyticsBucket = new s3.Bucket(this, 'SensorAnalyticsBucket', {
      bucketName: `${projectName}-sensor-analytics-${environment}`,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      lifecycleRules: [
        {
          id: 'DeleteAfter30Days',
          enabled: true,
          expiration: cdk.Duration.days(30),
        },
      ],
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    // Lambda execution role
    const lambdaExecutionRole = new iam.Role(this, 'LambdaExecutionRole', {
      roleName: `${projectName}-lambda-execution-${environment}`,
      assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'),
      ],
      inlinePolicies: {
        S3AccessPolicy: new iam.PolicyDocument({
          statements: [
            new iam.PolicyStatement({
              effect: iam.Effect.ALLOW,
              actions: ['s3:GetObject', 's3:PutObject'],
              resources: [`${sensorAnalyticsBucket.bucketArn}/*`],
            }),
          ],
        }),
      },
    });

    // Gyroscope analysis Lambda function
    const gyroscopeAnalysisFunction = new lambda.Function(this, 'GyroscopeAnalysisFunction', {
      functionName: `${projectName}-gyroscope-analysis-${environment}`,
      runtime: lambda.Runtime.PYTHON_3_9,
      handler: 'index.handler',
      role: lambdaExecutionRole,
      timeout: cdk.Duration.seconds(30),
      memorySize: 128,
      code: lambda.Code.fromInline(`
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
        
        # Store results (no personal data)
        result = {
            'timestamp': data.get('timestamp'),
            'stability_score': stability_score,
            'analysis_version': '1.0'
        }
        
        print(f"Stability analysis: {stability_score}")
    
    return {'statusCode': 200}

def calculate_stability(gyro_data):
    # Simple stability calculation
    movements = gyro_data.get('movements', [])
    if not movements:
        return 0.5
    
    total_magnitude = sum(m.get('magnitude', 0) for m in movements)
    avg_magnitude = total_magnitude / len(movements)
    
    # Convert to stability score (0-1, higher = more stable)
    return max(0, min(1, 1 - (avg_magnitude / 5.0)))
      `),
    });

    // Heart rate analysis Lambda function
    const heartRateAnalysisFunction = new lambda.Function(this, 'HeartRateAnalysisFunction', {
      functionName: `${projectName}-heartrate-analysis-${environment}`,
      runtime: lambda.Runtime.PYTHON_3_9,
      handler: 'index.handler',
      role: lambdaExecutionRole,
      timeout: cdk.Duration.seconds(30),
      memorySize: 128,
      code: lambda.Code.fromInline(`
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
        
        result = {
            'timestamp': data.get('timestamp'),
            'hrv_score': hrv_score,
            'analysis_version': '1.0'
        }
        
        print(f"HRV analysis: {hrv_score}")
    
    return {'statusCode': 200}

def calculate_hrv(hr_data):
    # Heart rate variability calculation
    bpm = hr_data.get('bpm', 70)
    resting_bpm = hr_data.get('resting_bpm', 70)
    
    # Calculate percentage change
    if resting_bpm > 0:
        change_percent = abs(bpm - resting_bpm) / resting_bpm
        return min(1.0, change_percent)
    
    return 0.0
      `),
    });

    // S3 bucket notifications
    sensorAnalyticsBucket.addEventNotification(
      s3.EventType.OBJECT_CREATED,
      new s3n.LambdaDestination(gyroscopeAnalysisFunction),
      { prefix: 'gyroscope/' }
    );

    sensorAnalyticsBucket.addEventNotification(
      s3.EventType.OBJECT_CREATED,
      new s3n.LambdaDestination(heartRateAnalysisFunction),
      { prefix: 'heartrate/' }
    );

    // CloudWatch Dashboard
    const dashboard = new cloudwatch.Dashboard(this, 'SensorAnalyticsDashboard', {
      dashboardName: `${projectName}-sensor-analytics-${environment}`,
      widgets: [
        [
          new cloudwatch.GraphWidget({
            title: 'Gyroscope Analysis Metrics',
            left: [
              gyroscopeAnalysisFunction.metricInvocations(),
              gyroscopeAnalysisFunction.metricDuration(),
              gyroscopeAnalysisFunction.metricErrors(),
            ],
            width: 12,
            height: 6,
          }),
        ],
        [
          new cloudwatch.GraphWidget({
            title: 'Heart Rate Analysis Metrics',
            left: [
              heartRateAnalysisFunction.metricInvocations(),
              heartRateAnalysisFunction.metricDuration(),
              heartRateAnalysisFunction.metricErrors(),
            ],
            width: 12,
            height: 6,
          }),
        ],
      ],
    });

    // Outputs
    new cdk.CfnOutput(this, 'SensorAnalyticsBucketName', {
      value: sensorAnalyticsBucket.bucketName,
      description: 'S3 bucket for sensor analytics',
      exportName: `${projectName}-sensor-bucket-${environment}`,
    });

    new cdk.CfnOutput(this, 'GyroscopeAnalysisFunctionArn', {
      value: gyroscopeAnalysisFunction.functionArn,
      description: 'Gyroscope analysis Lambda function ARN',
      exportName: `${projectName}-gyroscope-function-${environment}`,
    });

    new cdk.CfnOutput(this, 'DashboardURL', {
      value: `https://console.aws.amazon.com/cloudwatch/home?region=${this.region}#dashboards:name=${projectName}-sensor-analytics-${environment}`,
      description: 'CloudWatch Dashboard URL',
    });
  }
}
