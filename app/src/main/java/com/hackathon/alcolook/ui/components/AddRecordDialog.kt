package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit
import com.hackathon.alcolook.data.model.DrinkingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (DrinkType, DrinkUnit, Int, Float?, String?, String?) -> Unit
) {
    var showResultDialog by remember { mutableStateOf(false) }
    var resultStatus by remember { mutableStateOf(DrinkingStatus.APPROPRIATE) }
    var resultMessage by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(DrinkType.BEER) }
    var selectedUnit by remember { mutableStateOf(DrinkUnit.BOTTLE) }
    var quantity by remember { mutableStateOf("1") }
    var customAbv by remember { mutableStateOf("") }
    var customDrinkName by remember { mutableStateOf("") }
    var customVolume by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    // 기본 도수 표시
    val defaultAbv = selectedType.getDefaultAbv()
    val displayAbv = if (customAbv.isBlank()) defaultAbv.toString() else customAbv
    
    // 용량 계산
    val volumePerUnit = if (selectedUnit == DrinkUnit.OTHER) {
        customVolume.toIntOrNull() ?: 0
    } else {
        selectedUnit.getVolumeMl(selectedType)
    }
    val totalVolume = (quantity.toIntOrNull() ?: 1) * volumePerUnit

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "음주 기록 추가",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 음주 종류 선택 (8가지)
                item {
                    Text(
                        text = "음주 종류",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DrinkType.values().toList()) { type ->
                            FilterChip(
                                onClick = { selectedType = type },
                                label = { 
                                    Text(
                                        text = "${type.getEmoji()} ${type.getDisplayName()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                selected = selectedType == type
                            )
                        }
                    }
                }
                
                // 기타 술 이름 입력 (기타 선택 시만)
                if (selectedType == DrinkType.OTHER) {
                    item {
                        OutlinedTextField(
                            value = customDrinkName,
                            onValueChange = { customDrinkName = it },
                            label = { Text("술 이름") },
                            placeholder = { Text("예: 청하, 막걸리 등") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // 단위 선택 (5가지)
                item {
                    Text(
                        text = "단위",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DrinkUnit.values().toList()) { unit ->
                            FilterChip(
                                onClick = { selectedUnit = unit },
                                label = { 
                                    Text(
                                        text = if (unit == DrinkUnit.OTHER) {
                                            "${unit.getDisplayName()} (직접 입력)"
                                        } else {
                                            "${unit.getDisplayName()} (${unit.getVolumeMl(selectedType)}ml)"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                selected = selectedUnit == unit
                            )
                        }
                    }
                }


                // 기타 용량 입력 (기타 선택 시만)
                if (selectedUnit == DrinkUnit.OTHER) {
                    item {
                        OutlinedTextField(
                            value = customVolume,
                            onValueChange = { customVolume = it },
                            label = { Text("용량 (ml)") },
                            placeholder = { Text("예: 300") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                // 수량 입력
                item {
                    Text(
                        text = "수량",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("수량") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        Text(
                            text = "총 ${totalVolume}ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 도수 입력 (기본값 표시 + 수정 가능)
                item {
                    Text(
                        text = "도수 (선택사항)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = customAbv,
                        onValueChange = { customAbv = it },
                        label = { Text("도수 (%)") },
                        placeholder = { Text("기본값: ${defaultAbv}%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = {
                            Text("비워두면 기본 도수(${defaultAbv}%)가 적용됩니다")
                        }
                    )
                }

                // 메모 입력
                item {
                    Text(
                        text = "메모 (선택사항)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("메모") },
                        placeholder = { Text("예: 회식, 집들이 등") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                // 요약 정보
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "기록 요약",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${selectedType.getEmoji()} ${if (selectedType == DrinkType.OTHER && customDrinkName.isNotBlank()) customDrinkName else selectedType.getDisplayName()} ${quantity}${selectedUnit.getDisplayName()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val pureAlcohol = totalVolume * (customAbv.toFloatOrNull() ?: selectedType.getDefaultAbv()) * 0.789f / 100f
                            val standardDrinks = pureAlcohol / 8f
                            Text(
                                text = "총 용량: ${totalVolume}ml, 도수: ${displayAbv}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "순수 알코올: ${String.format("%.1f", pureAlcohol)}g (표준잔 ${String.format("%.1f", standardDrinks)}잔)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 버튼
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("취소")
                        }
                        
                        Button(
                            onClick = {
                                val qty = quantity.toIntOrNull() ?: 1
                                val abv = customAbv.toFloatOrNull()
                                val drinkName = if (selectedType == DrinkType.OTHER && customDrinkName.isNotBlank()) customDrinkName else null
                                val memo = if (note.isNotBlank()) note else null
                                
                                // 상태 평가
                                val pureAlcohol = totalVolume * (customAbv.toFloatOrNull() ?: selectedType.getDefaultAbv()) * 0.789f / 100f
                                val isMale = true // TODO: 사용자 성별 가져오기
                                
                                resultStatus = when {
                                    pureAlcohol <= if (isMale) 28f else 14f -> DrinkingStatus.APPROPRIATE
                                    pureAlcohol <= if (isMale) 56f else 42f -> DrinkingStatus.CAUTION
                                    pureAlcohol <= if (isMale) 70f else 56f -> DrinkingStatus.EXCESSIVE
                                    else -> DrinkingStatus.DANGEROUS
                                }
                                
                                // 랜덤 메시지 선택
                                val messages = when (resultStatus) {
                                    DrinkingStatus.APPROPRIATE -> listOf(
                                        "오늘은 딱 알맞게 즐기셨네요! 균형 잡힌 음주, 멋져요!",
                                        "좋습니다 내일도 상쾌하게 일어날 수 있겠네요.",
                                        "이 정도면 건강에 큰 무리 없어요. 현명한 선택이네요!",
                                        "오늘은 깔끔하게 딱 적정량만! 자기 관리 잘하시네요"
                                    )
                                    DrinkingStatus.CAUTION -> listOf(
                                        "조금은 과했네요 내일은 물 많이 드시고 쉬어주세요.",
                                        "이 정도면 괜찮지만, 매일 반복되면 몸이 힘들 수 있어요.",
                                        "슬슬 간이 피곤해질지도… 내일은 가볍게 보내는 게 어떨까요?",
                                        "컨디션 체크하면서 마시는 것도 중요해요"
                                    )
                                    DrinkingStatus.EXCESSIVE -> listOf(
                                        "이건 위험한 수준이에요 속도를 줄이셔야 합니다.",
                                        "오늘은 좀 과격했네요… 간이 놀랐을 거예요",
                                        "이러다 내일 숙취와 함께 고통받을 수도 있어요",
                                        "가끔은 괜찮지만, 자주 반복되면 건강에 큰 부담이 돼요."
                                    )
                                    DrinkingStatus.DANGEROUS -> listOf(
                                        "심각한 음주 패턴이 보입니다 전문가 상담을 고려하세요."
                                    )
                                }
                                resultMessage = messages.random()
                                
                                showResultDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("추가")
                        }
                    }
                }
            }
        }
        
        // 결과 다이얼로그
        if (showResultDialog) {
            Dialog(onDismissRequest = { }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when(resultStatus) {
                            DrinkingStatus.APPROPRIATE -> Color(0xFFE8F5E8)
                            DrinkingStatus.CAUTION -> Color(0xFFFFF4E5)
                            DrinkingStatus.EXCESSIVE -> Color(0xFFFDEBEC)
                            DrinkingStatus.DANGEROUS -> Color(0xFFFFE0E0)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🐕",
                            fontSize = 48.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = when(resultStatus) {
                                DrinkingStatus.APPROPRIATE -> "적정"
                                DrinkingStatus.CAUTION -> "주의"
                                DrinkingStatus.EXCESSIVE -> "과음"
                                DrinkingStatus.DANGEROUS -> "위험"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when(resultStatus) {
                                DrinkingStatus.APPROPRIATE -> Color.Black
                                DrinkingStatus.CAUTION -> Color.Black
                                DrinkingStatus.EXCESSIVE -> Color.Black
                                DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = resultMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                val qty = quantity.toIntOrNull() ?: 1
                                val abv = customAbv.toFloatOrNull()
                                val drinkName = if (selectedType == DrinkType.OTHER && customDrinkName.isNotBlank()) customDrinkName else null
                                val memo = if (note.isNotBlank()) note else null
                                onConfirm(selectedType, selectedUnit, qty, abv, drinkName, memo)
                                showResultDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("기록 저장")
                        }
                    }
                }
            }
        }
    }
}