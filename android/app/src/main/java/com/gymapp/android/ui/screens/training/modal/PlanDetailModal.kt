package com.gymapp.android.ui.screens.training.modal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.android.data.remote.response.PlanExerciseResponse
import com.gymapp.android.data.remote.response.WorkoutPlanResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailModal(
    plan: WorkoutPlanResponse,
    onDismiss: () -> Unit
) {
    var selectedExercise by remember { mutableStateOf<PlanExerciseResponse?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(4.dp))

            if (!plan.scheduledDate.isNullOrEmpty()) {
                Text(
                    "📅 ${plan.scheduledDate}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Danh sách bài tập:", fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(8.dp))

            plan.exercises.forEach { ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedExercise = ex },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(ex.exerciseName, fontWeight = FontWeight.Medium)
                            Text(
                                "${ex.sets} sets × ${ex.reps} reps · nghỉ ${ex.restSeconds}s",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
            }
        }
    }

    selectedExercise?.let { ex ->
        ExerciseInfoDialog(exercise = ex, onDismiss = { selectedExercise = null })
    }
}