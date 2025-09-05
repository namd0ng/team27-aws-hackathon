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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (DrinkType, DrinkUnit, Int, Float?, String?, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(DrinkType.BEER) }
    var selectedUnit by remember { mutableStateOf(DrinkUnit.BOTTLE) }
    var quantity by remember { mutableStateOf("1") }
    var customAbv by remember { mutableStateOf("") }
    var customDrinkName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    // 기본 도수 표시
    val defaultAbv = selectedType.getDefaultAbv()
    val displayAbv = if (customAbv.isBlank()) defaultAbv.toString() else customAbv
    
    // 용량 계산
    val volumePerUnit = selectedUnit.getVolumeMl(selectedType)
    val totalVolume = (quantity.toIntOrNull() ?: 1) * volumePerUnit

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
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
                                        text = "${unit.getDisplayName()} (${unit.getVolumeMl(selectedType)}ml)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                selected = selectedUnit == unit
                            )
                        }
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
                                onConfirm(selectedType, selectedUnit, qty, abv, drinkName, memo)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("추가")
                        }
                    }
                }
            }
        }
    }
}