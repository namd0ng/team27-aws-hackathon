import * as cdk from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export interface AlcoLookStackProps extends cdk.StackProps {
  environment: string;
  projectName: string;
}

export class AlcoLookStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: AlcoLookStackProps) {
    super(scope, id, props);

    const { environment, projectName } = props;

    // IAM Role for Rekognition
    const rekognitionRole = new iam.Role(this, 'RekognitionRole', {
      roleName: `${projectName}-rekognition-role-${environment}`,
      assumedBy: new iam.ServicePrincipal('rekognition.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonRekognitionFullAccess'),
      ],
    });

    // IAM Role for Bedrock
    const bedrockRole = new iam.Role(this, 'BedrockRole', {
      roleName: `${projectName}-bedrock-role-${environment}`,
      assumedBy: new iam.ServicePrincipal('bedrock.amazonaws.com'),
      inlinePolicies: {
        BedrockInvokePolicy: new iam.PolicyDocument({
          statements: [
            new iam.PolicyStatement({
              effect: iam.Effect.ALLOW,
              actions: [
                'bedrock:InvokeModel',
                'bedrock:InvokeModelWithResponseStream',
              ],
              resources: ['*'],
            }),
          ],
        }),
      },
    });

    // S3 Bucket for temporary image storage
    const imageBucket = new s3.Bucket(this, 'ImageStorageBucket', {
      bucketName: `${projectName}-images-${environment}-${this.account}`,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      lifecycleRules: [
        {
          id: 'delete-temp-images',
          enabled: true,
          expiration: cdk.Duration.days(1),
        },
      ],
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      autoDeleteObjects: true,
    });

    // CloudWatch Log Group
    const logGroup = new logs.LogGroup(this, 'ApplicationLogGroup', {
      logGroupName: `/aws/alcolook/${environment}`,
      retention: logs.RetentionDays.ONE_MONTH,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    // Outputs
    new cdk.CfnOutput(this, 'RekognitionRoleArn', {
      value: rekognitionRole.roleArn,
      description: 'ARN of the Rekognition service role',
      exportName: `${projectName}-rekognition-role-${environment}`,
    });

    new cdk.CfnOutput(this, 'BedrockRoleArn', {
      value: bedrockRole.roleArn,
      description: 'ARN of the Bedrock service role',
      exportName: `${projectName}-bedrock-role-${environment}`,
    });

    new cdk.CfnOutput(this, 'ImageBucketName', {
      value: imageBucket.bucketName,
      description: 'Name of the S3 bucket for image storage',
      exportName: `${projectName}-image-bucket-${environment}`,
    });

    new cdk.CfnOutput(this, 'LogGroupName', {
      value: logGroup.logGroupName,
      description: 'Name of the CloudWatch log group',
      exportName: `${projectName}-log-group-${environment}`,
    });
  }
}
