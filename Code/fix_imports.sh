#!/bin/bash

# Material3 임포트 충돌 해결 스크립트
# RoundedCornerShape 충돌을 해결하기 위해 material3.* 를 개별 임포트로 변경

echo "Material3 임포트 충돌 해결 중..."

# 모든 Kotlin 파일에서 material3.* 임포트를 개별 임포트로 변경
find /mnt/c/Users/admin/Downloads/team27-aws-hackathon/app/src -name "*.kt" -exec sed -i 's/import androidx.compose.material3.\*/import androidx.compose.material3.Box\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.Column\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.FilterChip\nimport androidx.compose.material3.FilterChipDefaults\nimport androidx.compose.material3.HorizontalDivider\nimport androidx.compose.material3.IconButton\nimport androidx.compose.material3.LinearProgressIndicator\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.OutlinedButton\nimport androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.Row\nimport androidx.compose.material3.Scaffold\nimport androidx.compose.material3.Surface\nimport androidx.compose.material3.Tab\nimport androidx.compose.material3.TabRow\nimport androidx.compose.material3.TabRowDefaults\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.TextButton\nimport androidx.compose.material3.TextField\nimport androidx.compose.material3.TopAppBar/' {} \;

echo "임포트 충돌 해결 완료!"
