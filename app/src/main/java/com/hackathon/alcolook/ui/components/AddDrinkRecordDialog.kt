package com.hackathon.alcolook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.hackathon.alcolook.data.model.DrinkType
import com.hackathon.alcolook.data.model.DrinkUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDrinkRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (DrinkType, DrinkUnit, Int, Float?, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(DrinkType.SOJU) }
    var selectedUnit by remember { mutableStateOf(DrinkUnit.GLASS) }
    var quantity by remember { mutableStateOf("1") }
    var customAbv by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "음주 기록 추가",
                    style = MaterialTheme.typography.titleLarge
                )
                
                // 술 종류
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedType.getDisplayName(),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("술 종류") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 단위
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedUnit.getDisplayName(),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("단위") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 수량
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("수량") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 도수 (선택사항)
                OutlinedTextField(
                    value = customAbv,
                    onValueChange = { customAbv = it },
                    label = { Text("도수 (선택사항)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 메모
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("메모") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull() ?: 1
                            val abv = customAbv.toFloatOrNull()
                            onConfirm(selectedType, selectedUnit, qty, abv, note.ifBlank { null })
                        }
                    ) {
                        Text("추가")
                    }
                }
            }
        }
    }
}