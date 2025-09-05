package com.hackathon.alcolook.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon.alcolook.data.model.Gender
import com.hackathon.alcolook.data.model.UserProfile

@Composable
fun ProfileEditDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var sex by remember { mutableStateOf(userProfile.sex) }
    var isSenior65 by remember { mutableStateOf(userProfile.isSenior65) }
    var weeklyGoal by remember { mutableStateOf(userProfile.weeklyGoalStdDrinks?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로필 편집") },
        text = {
            Column {
                Text("성별", style = MaterialTheme.typography.labelMedium)
                Row {
                    FilterChip(
                        onClick = { sex = Gender.MALE },
                        label = { Text("남성") },
                        selected = sex == Gender.MALE
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        onClick = { sex = Gender.FEMALE },
                        label = { Text("여성") },
                        selected = sex == Gender.FEMALE
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSenior65,
                        onCheckedChange = { isSenior65 = it }
                    )
                    Text("65세 이상")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = weeklyGoal,
                    onValueChange = { weeklyGoal = it },
                    label = { Text("주간 목표 (잔)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedProfile = UserProfile(
                        sex = sex,
                        isSenior65 = isSenior65,
                        weeklyGoalStdDrinks = weeklyGoal.toIntOrNull()
                    )
                    onSave(updatedProfile)
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
