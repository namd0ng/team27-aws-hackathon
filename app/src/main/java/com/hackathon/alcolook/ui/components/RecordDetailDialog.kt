package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hackathon.alcolook.data.model.DrinkRecord
import com.hackathon.alcolook.data.model.DrinkingStatus
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailDialog(
    records: List<DrinkRecord>,
    selectedDate: java.time.LocalDate,
    dailyStatus: DrinkingStatus,
    totalAlcohol: Float,
    totalStandardDrinks: Float,
    onDismiss: () -> Unit,
    onEditRecord: (DrinkRecord) -> Unit,
    onDeleteRecord: (DrinkRecord) -> Unit
) {
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
                        text = "${selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 상세 기록",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 요약 정보
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when(dailyStatus) {
                                DrinkingStatus.APPROPRIATE -> androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.1f)
                                DrinkingStatus.CAUTION -> androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.1f)
                                DrinkingStatus.EXCESSIVE -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
                                DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "일일 요약",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("총 음주량: ${String.format("%.1f", totalAlcohol)}g")
                                Text("표준잔수: ${String.format("%.1f", totalStandardDrinks)}잔")
                            }
                            Text(
                                text = "상태: ${when(dailyStatus) {
                                    DrinkingStatus.APPROPRIATE -> "적정"
                                    DrinkingStatus.CAUTION -> "주의"
                                    DrinkingStatus.EXCESSIVE -> "과음"
                                    DrinkingStatus.DANGEROUS -> "위험"
                                }}",
                                fontWeight = FontWeight.SemiBold,
                                color = when(dailyStatus) {
                                    DrinkingStatus.APPROPRIATE -> androidx.compose.ui.graphics.Color.Green
                                    DrinkingStatus.CAUTION -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                                    DrinkingStatus.EXCESSIVE -> androidx.compose.ui.graphics.Color.Red
                                    DrinkingStatus.DANGEROUS -> androidx.compose.ui.graphics.Color.Black
                                }
                            )
                        }
                    }
                }
                
                item {
                    Text(
                        text = "개별 기록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 개별 기록 목록
                items(records) { record ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${record.type.getEmoji()} ${record.type.getDisplayName()} ${record.quantity}${record.unit.getDisplayName()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "순수 알코올 ${String.format("%.1f", record.getPureAlcoholGrams())}g (표준잔 ${record.getFormattedStandardDrinks()}잔)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (record.note?.isNotBlank() == true) {
                                        Text(
                                            text = "📝 ${record.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                Row {
                                    TextButton(onClick = { onEditRecord(record) }) {
                                        Text("수정", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { onDeleteRecord(record) }) {
                                        Text("삭제", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("닫기")
                    }
                }
            }
        }
    }
}