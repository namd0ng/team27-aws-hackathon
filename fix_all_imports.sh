#!/bin/bash

echo "전체 프로젝트 임포트 오류 해결 중..."

# 모든 Kotlin 파일 찾기
find /mnt/c/Users/admin/Downloads/team27-aws-hackathon/app/src -name "*.kt" | while read file; do
    echo "Processing: $file"
    
    # CircularProgressIndicator 임포트 추가
    if grep -q "CircularProgressIndicator" "$file" && ! grep -q "import androidx.compose.material3.CircularProgressIndicator" "$file"; then
        sed -i '/import androidx.compose.material3.Card/a import androidx.compose.material3.CircularProgressIndicator' "$file"
    fi
    
    # Icon 임포트 추가
    if grep -q "Icon(" "$file" && ! grep -q "import androidx.compose.material3.Icon" "$file"; then
        sed -i '/import androidx.compose.material3.HorizontalDivider/a import androidx.compose.material3.Icon' "$file"
    fi
    
    # RadioButton 임포트 추가
    if grep -q "RadioButton" "$file" && ! grep -q "import androidx.compose.material3.RadioButton" "$file"; then
        sed -i '/import androidx.compose.material3.OutlinedTextField/a import androidx.compose.material3.RadioButton' "$file"
    fi
    
    # Divider를 HorizontalDivider로 변경
    sed -i 's/Divider(/HorizontalDivider(/g' "$file"
    
    # Checkbox 임포트 추가
    if grep -q "Checkbox" "$file" && ! grep -q "import androidx.compose.material3.Checkbox" "$file"; then
        sed -i '/import androidx.compose.material3.Card/a import androidx.compose.material3.Checkbox' "$file"
    fi
    
    # Switch 임포트 추가
    if grep -q "Switch" "$file" && ! grep -q "import androidx.compose.material3.Switch" "$file"; then
        sed -i '/import androidx.compose.material3.Surface/a import androidx.compose.material3.Switch' "$file"
    fi
    
    # Slider 임포트 추가
    if grep -q "Slider" "$file" && ! grep -q "import androidx.compose.material3.Slider" "$file"; then
        sed -i '/import androidx.compose.material3.Surface/a import androidx.compose.material3.Slider' "$file"
    fi
    
    # DropdownMenu 임포트 추가
    if grep -q "DropdownMenu" "$file" && ! grep -q "import androidx.compose.material3.DropdownMenu" "$file"; then
        sed -i '/import androidx.compose.material3.Card/a import androidx.compose.material3.DropdownMenu\nimport androidx.compose.material3.DropdownMenuItem' "$file"
    fi
    
    # AlertDialog 임포트 추가
    if grep -q "AlertDialog" "$file" && ! grep -q "import androidx.compose.material3.AlertDialog" "$file"; then
        sed -i '/import androidx.compose.material3.Button/a import androidx.compose.material3.AlertDialog' "$file"
    fi
    
    # FloatingActionButton 임포트 추가
    if grep -q "FloatingActionButton" "$file" && ! grep -q "import androidx.compose.material3.FloatingActionButton" "$file"; then
        sed -i '/import androidx.compose.material3.FilterChip/a import androidx.compose.material3.FloatingActionButton' "$file"
    fi
    
    # Snackbar 임포트 추가
    if grep -q "Snackbar" "$file" && ! grep -q "import androidx.compose.material3.Snackbar" "$file"; then
        sed -i '/import androidx.compose.material3.Surface/a import androidx.compose.material3.Snackbar\nimport androidx.compose.material3.SnackbarHost\nimport androidx.compose.material3.SnackbarHostState' "$file"
    fi
    
    # TopAppBar 임포트 추가
    if grep -q "TopAppBar" "$file" && ! grep -q "import androidx.compose.material3.TopAppBar" "$file"; then
        sed -i '/import androidx.compose.material3.TextButton/a import androidx.compose.material3.TopAppBar\nimport androidx.compose.material3.TopAppBarDefaults' "$file"
    fi
    
    # BottomAppBar 임포트 추가
    if grep -q "BottomAppBar" "$file" && ! grep -q "import androidx.compose.material3.BottomAppBar" "$file"; then
        sed -i '/import androidx.compose.material3.Button/a import androidx.compose.material3.BottomAppBar' "$file"
    fi
    
    # NavigationBar 임포트 추가
    if grep -q "NavigationBar" "$file" && ! grep -q "import androidx.compose.material3.NavigationBar" "$file"; then
        sed -i '/import androidx.compose.material3.MaterialTheme/a import androidx.compose.material3.NavigationBar\nimport androidx.compose.material3.NavigationBarItem' "$file"
    fi
    
done

echo "임포트 오류 해결 완료!"
