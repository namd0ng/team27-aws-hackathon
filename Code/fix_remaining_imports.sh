#!/bin/bash

echo "추가 임포트 문제 해결 중..."

# 모든 Kotlin 파일 처리
find /mnt/c/Users/admin/Downloads/team27-aws-hackathon/app/src -name "*.kt" | while read file; do
    
    # Column, Row, Spacer 등 기본 레이아웃 임포트 추가
    if grep -q "Column\|Row\|Spacer\|Box" "$file" && ! grep -q "import androidx.compose.foundation.layout.Column" "$file"; then
        sed -i '1a import androidx.compose.foundation.layout.*' "$file"
    fi
    
    # LazyColumn, LazyRow 임포트 추가
    if grep -q "LazyColumn\|LazyRow" "$file" && ! grep -q "import androidx.compose.foundation.lazy" "$file"; then
        sed -i '1a import androidx.compose.foundation.lazy.*' "$file"
    fi
    
    # remember, mutableStateOf 등 런타임 임포트 추가
    if grep -q "remember\|mutableStateOf\|State" "$file" && ! grep -q "import androidx.compose.runtime" "$file"; then
        sed -i '1a import androidx.compose.runtime.*' "$file"
    fi
    
    # Modifier 임포트 추가
    if grep -q "Modifier\." "$file" && ! grep -q "import androidx.compose.ui.Modifier" "$file"; then
        sed -i '1a import androidx.compose.ui.Modifier' "$file"
    fi
    
    # Alignment 임포트 추가
    if grep -q "Alignment\." "$file" && ! grep -q "import androidx.compose.ui.Alignment" "$file"; then
        sed -i '1a import androidx.compose.ui.Alignment' "$file"
    fi
    
    # Color 임포트 추가
    if grep -q "Color\." "$file" && ! grep -q "import androidx.compose.ui.graphics.Color" "$file"; then
        sed -i '1a import androidx.compose.ui.graphics.Color' "$file"
    fi
    
    # dp, sp 단위 임포트 추가
    if grep -q "\.dp\|\.sp" "$file" && ! grep -q "import androidx.compose.ui.unit" "$file"; then
        sed -i '1a import androidx.compose.ui.unit.*' "$file"
    fi
    
    # FontWeight 임포트 추가
    if grep -q "FontWeight\." "$file" && ! grep -q "import androidx.compose.ui.text.font.FontWeight" "$file"; then
        sed -i '1a import androidx.compose.ui.text.font.FontWeight' "$file"
    fi
    
done

echo "추가 임포트 문제 해결 완료!"
