package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.alcolook.ui.theme.*

/**
 * 설정 화면용 드롭다운 아이템 컴포넌트
 * ListItem 기반으로 구현하여 캘린더 탭과 동일한 스타일 적용
 * 
 * @param emoji 아이템 앞에 표시할 이모지
 * @param title 설정 항목 제목
 * @param options 선택 가능한 옵션 목록
 * @param selectedValue 현재 선택된 값
 * @param onValueChange 값 변경 콜백
 * @param modifier 컴포저블 수정자
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSettingsItem(
    emoji: String,
    title: String,
    options: List<String>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 드롭다운 메뉴 확장 상태 관리
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // ListItem을 사용하여 캘린더 탭과 동일한 스타일 적용
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            },
            leadingContent = {
                // 이모지 아이콘 (Material Icons 대신 플랫폼 이모지 사용)
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            },
            trailingContent = {
                // 선택 버튼 (캘린더 탭 버튼 스타일과 동일)
                OutlinedButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TabSelected,
                        containerColor = Color.Transparent
                    ),

                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = selectedValue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
        
        // Material3 DropdownMenu 사용
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    color = CardBackground,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = if (option == selectedValue) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = MenuDefaults.itemColors(
                        textColor = TextPrimary,
                        leadingIconColor = TextPrimary,
                        trailingIconColor = TextPrimary
                    )
                )
            }
        }
    }
}

/**
 * 설정 값을 저장하는 상태 홀더
 * rememberSaveable을 사용하여 화면 회전 등에서도 값 유지
 */
@Composable
fun rememberSettingsState(
    initialValue: String
): MutableState<String> {
    return rememberSaveable { mutableStateOf(initialValue) }
}