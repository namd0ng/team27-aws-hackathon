#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { AlcoLookStack } from './lib/alcolook-stack';

const app = new cdk.App();

const environment = app.node.tryGetContext('environment') || 'dev';
const projectName = app.node.tryGetContext('projectName') || 'alcolook';

new AlcoLookStack(app, `${projectName}-stack-${environment}`, {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || 'us-east-1',
  },
  environment,
  projectName,
  tags: {
    Project: projectName,
    Environment: environment,
    ManagedBy: 'CDK',
  },
});
