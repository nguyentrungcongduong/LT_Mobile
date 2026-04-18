package com.gymapp.android.ui.screens.training

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gymapp.android.data.remote.dto.PlanExerciseRequest
import com.gymapp.android.domain.model.workout.ExerciseUI
import com.gymapp.android.ui.screens.training.modal.ExerciseConfigModal
import com.gymapp.android.ui.screens.training.modal.ExercisePickerModal
import java.time.LocalDate
import java.util.Calendar

@Composable
fun CreateScheduleScreen(navController: NavController) {

    val viewModel: WorkoutViewModel = hiltViewModel()
    val context = LocalContext.current

    var planName by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf("") }
    var showModal by remember { mutableStateOf(false) }

    // exercise đã chọn kèm config sets/reps
    var selectedExercises by remember { mutableStateOf(listOf<PlanExerciseRequest>()) }
    var selectedExerciseNames by remember { mutableStateOf(mapOf<String, String>()) } // id -> name

    // popup config cho bài vừa chọn
    var pendingExercise by remember { mutableStateOf<ExerciseUI?>(null) }

    LaunchedEffect(Unit) { viewModel.loadExercises() }

    // 👉 DATE PICKER
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            scheduledDate = "%04d-%02d-%02d".format(year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Trở lại")
            }
            Text("Tạo lịch tập", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TÊN LỊCH
        OutlinedTextField(
            value = planName,
            onValueChange = { planName = it },
            label = { Text("Tên lịch tập") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CHỌN NGÀY
        OutlinedTextField(
            value = scheduledDate.ifEmpty { "Chọn ngày tập" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Ngày tập") },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 👉 THÊM BÀI TẬP
        Button(
            onClick = { showModal = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Thêm bài tập")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 👉 LIST ĐÃ CHỌN
        if (selectedExercises.isNotEmpty()) {
            Text("Bài tập đã chọn:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            selectedExercises.forEachIndexed { index, ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(selectedExerciseNames[ex.exerciseId] ?: ex.exerciseId, fontWeight = FontWeight.Medium)
                            Text("${ex.sets} sets × ${ex.reps} reps · nghỉ ${ex.restSeconds}s", fontSize = 13.sp)
                        }
                        IconButton(onClick = {
                            selectedExercises = selectedExercises.toMutableList().also { it.removeAt(index) }
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Xoá")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 👉 LƯU
        Button(
            onClick = {
                if (planName.isBlank() || selectedExercises.isEmpty()) return@Button
                viewModel.createPlan(
                    name = planName,
                    scheduledDate = scheduledDate.ifEmpty { null },
                    exercises = selectedExercises
                )
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 Lưu lịch tập")
        }
    }

    // 👉 MODAL CHỌN BÀI TẬP
    if (showModal) {
        ExercisePickerModal(
            exercises = viewModel.exercises,
            onDismiss = { showModal = false },
            onConfirm = { picked ->
                // Mở popup config cho bài đầu tiên chưa được config
                // Ở đây đơn giản: mở config cho từng bài một
                picked.forEach { ex ->
                    selectedExerciseNames = selectedExerciseNames + (ex.id to ex.name)
                }
                // Trigger config popup cho bài đầu tiên
                pendingExercise = picked.firstOrNull()
                showModal = false
            }
        )
    }

    // 👉 POPUP SETS/REPS
    pendingExercise?.let { ex ->
        ExerciseConfigModal(
            exercise = ex,
            onDismiss = { pendingExercise = null },
            onConfirm = { sets, reps, rest ->
                val newEntry = PlanExerciseRequest(
                    exerciseId = ex.id,
                    sets = sets,
                    reps = reps,
                    restSeconds = rest,
                    orderIndex = selectedExercises.size
                )
                selectedExercises = selectedExercises + newEntry
                pendingExercise = null
            }
        )
    }
}