package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onEditRecord: ((DrinkRecord) -> Unit)? = null,
    onDeleteRecord: (DrinkRecord) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 헤더
                item {
                    Text(
                        text = "${selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 상세 기록",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 일일 요약 제목
                item {
                    Text(
                        text = "일일 요약",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 일일 요약 카드
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when (dailyStatus) {
                                DrinkingStatus.APPROPRIATE -> Color(0xFFE8F5E8)
                                DrinkingStatus.CAUTION -> Color(0xFFFFF4E5)
                                DrinkingStatus.EXCESSIVE -> Color(0xFFFDEBEC)
                                DrinkingStatus.DANGEROUS -> Color(0xFFFFE0E0)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "총 음주량: ${String.format("%.1f", totalAlcohol)}g",
                                color = Color.Black
                            )
                            Text(
                                text = "표준잔수: ${String.format("%.1f", totalStandardDrinks)}잔",
                                color = Color.Black
                            )
                        }
                    }
                }

                // 측정 기록 제목
                item {
                    Text(
                        text = "음주 측정 기록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 측정 기록 목록
                items(records) { record ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                    if (onEditRecord != null) {
                                        TextButton(onClick = { onEditRecord(record) }) {
                                            Text("수정", fontSize = 12.sp)
                                        }
                                    }
                                    TextButton(onClick = { onDeleteRecord(record) }) {
                                        Text("삭제", fontSize = 12.sp, color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }

                // 닫기 버튼
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