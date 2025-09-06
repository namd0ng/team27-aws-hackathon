#!/bin/bash
cd /mnt/c/Users/admin/Downloads/team27-aws-hackathon
echo "Starting quick build..."
./gradlew clean assembleDebug --stacktrace --info | grep -E "(FAILED|error|Error|BUILD|Task)"
