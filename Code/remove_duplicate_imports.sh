#!/bin/bash

echo "중복 임포트 제거 중..."

# 모든 Kotlin 파일에서 중복 임포트 제거
find /mnt/c/Users/admin/Downloads/team27-aws-hackathon/app/src -name "*.kt" | while read file; do
    # 임시 파일 생성
    temp_file=$(mktemp)
    
    # 중복 제거 (sort -u는 정렬하면서 중복 제거)
    awk '!seen[$0]++' "$file" > "$temp_file"
    
    # 원본 파일에 덮어쓰기
    mv "$temp_file" "$file"
done

echo "중복 임포트 제거 완료!"
