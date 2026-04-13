package com.gymapp.android.ui.screens.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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

@Composable
fun ExerciseItem(
    exercise: PlanExerciseResponse,
    isChecked: Boolean,
    onCheck: () -> Unit
) {
    var showDetail by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { showDetail = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onCheck() }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = exercise.exerciseName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Text(
                    text = "${exercise.sets} sets x ${exercise.reps} reps",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }

    // MODAL chi tiết
    if (showDetail) {
        ExerciseDetailDialog(
            exercise = exercise,
            onDismiss = { showDetail = false }
        )
    }
}