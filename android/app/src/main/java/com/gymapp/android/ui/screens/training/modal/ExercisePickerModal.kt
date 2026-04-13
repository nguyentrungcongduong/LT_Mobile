package com.gymapp.android.ui.screens.training.modal

import android.widget.Button
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymapp.android.domain.model.workout.ExerciseUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerModal(
    exercises: List<ExerciseUI>,
    onDismiss: () -> Unit,
    onConfirm: (List<ExerciseUI>) -> Unit
) {
    var localList by remember { mutableStateOf(exercises) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text("Chọn bài tập", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(localList) { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                localList = localList.map {
                                    if (it.id == ex.id)
                                        it.copy(isSelected = !it.isSelected)
                                    else it
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(ex.name)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(if (ex.isSelected) "✔" else "")
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val selected = localList.filter { it.isSelected }
                    onConfirm(selected)
                }
            ) {
                Text("OK (${localList.count { it.isSelected }})")
            }
        }
    }
}