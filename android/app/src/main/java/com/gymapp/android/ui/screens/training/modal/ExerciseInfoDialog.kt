package com.gymapp.android.ui.screens.training.modal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymapp.android.data.remote.response.PlanExerciseResponse

@Composable
fun ExerciseInfoDialog(
    exercise: PlanExerciseResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        },
        title = {
            Text(exercise.exerciseName, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                exercise.muscleGroup?.let { Text("💪 Nhóm cơ: $it") }
                Text("🔁 ${exercise.sets} sets × ${exercise.reps} reps")
                Text("⏱ Nghỉ: ${exercise.restSeconds}s")
                exercise.description?.let { Text("📝 $it") }
                exercise.notes?.let { Text("🗒 Ghi chú: $it") }
                exercise.videoUrl?.let {
                    Text("🎥 Video: $it", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}