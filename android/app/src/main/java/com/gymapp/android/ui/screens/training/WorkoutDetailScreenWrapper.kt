package com.gymapp.android.ui.screens.training

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun WorkoutDetailScreenWrapper(planId: String) {
    val viewModel: WorkoutViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    val plan =
        viewModel.customPlans.find { it.id.toString() == planId }
            ?: viewModel.recommendedPlans.find { it.id.toString() == planId }
            ?: viewModel.ptPlans.find { it.id.toString() == planId }

    if (plan == null) {
        Text("Loading...")
        return
    }

    WorkoutDetailScreen(plan)
}