package com.gymapp.android.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal
import com.gymapp.android.ui.components.spinner.DropdownSelector

@Composable
fun GoalScreen(
    onDone: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Thiết lập mục tiêu", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(24.dp))

        DropdownSelector(
            label = "Kinh nghiệm",
            options = ExperienceLevel.values().toList(),
            selected = viewModel.experienceLevel,
            onSelect = { viewModel.experienceLevel = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DropdownSelector(
            label = "Mục tiêu",
            options = FitnessGoal.values().toList(),
            selected = viewModel.fitnessGoal,
            onSelect = { viewModel.fitnessGoal = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        viewModel.errorMessage?.let {
            Text(it, color = Color.Red)
        }

        Button(
            onClick = { viewModel.submit(onDone) },
            enabled = !viewModel.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Xác nhận")
            }
        }
    }
}