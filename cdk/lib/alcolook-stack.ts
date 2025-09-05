import * as cdk from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';
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

    // IAM Role for Rekognition (Real-time analysis only)
    const rekognitionRole = new iam.Role(this, 'RekognitionRole', {
      roleName: `${projectName}-rekognition-role-${environment}`,
      assumedBy: new iam.ServicePrincipal('rekognition.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('AmazonRekognitionReadOnlyAccess'),
      ],
    });

    // IAM Role for Bedrock (AI Analysis)
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

    // CloudWatch Log Group (Anonymous analytics only)
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

    new cdk.CfnOutput(this, 'LogGroupName', {
      value: logGroup.logGroupName,
      description: 'Name of the CloudWatch log group',
      exportName: `${projectName}-log-group-${environment}`,
    });
  }
}
