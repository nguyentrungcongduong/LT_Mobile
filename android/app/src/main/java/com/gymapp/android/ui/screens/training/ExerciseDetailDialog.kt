package com.gymapp.android.ui.screens.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymapp.android.data.remote.response.PlanExerciseResponse

@Composable
fun ExerciseDetailDialog(
    exercise: PlanExerciseResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Text(exercise.exerciseName)
        },
        text = {
            Column {
                Text("Nhóm cơ: ${exercise.muscleGroup}")
                Text("Sets: ${exercise.sets}")
                Text("Reps: ${exercise.reps}")
                Text("Nghỉ: ${exercise.restSeconds}s")

                exercise.notes?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ghi chú: $it")
                }
            }
        }
    )
}