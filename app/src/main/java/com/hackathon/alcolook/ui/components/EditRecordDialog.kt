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
import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordDialog(
    record: DrinkRecord,
    onDismiss: () -> Unit,
    onConfirm: (DrinkType, DrinkUnit, Int, Float?, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(record.type) }
    var selectedUnit by remember { mutableStateOf(record.unit) }
    var quantity by remember { mutableStateOf(record.quantity.toString()) }
    var customAbv by remember { mutableStateOf(record.abv?.toString() ?: "") }
    var note by remember { mutableStateOf(record.note ?: "") }
    
    val defaultAbv = selectedType.getDefaultAbv()
    val displayAbv = if (customAbv.isBlank()) defaultAbv.toString() else customAbv
    val volumePerUnit = selectedUnit.getVolumeMl(selectedType)
    val totalVolume = (quantity.toIntOrNull() ?: 1) * volumePerUnit

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "기록 수정",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                        singleLine = true
                    )
                }

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
                                val memo = if (note.isNotBlank()) note else null
                                onConfirm(selectedType, selectedUnit, qty, abv, memo)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("수정")
                        }
                    }
                }
            }
        }
    }
}