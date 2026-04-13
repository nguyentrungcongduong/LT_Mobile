package com.gymapp.android.ui.screens.training

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun WorkoutDetailScreenWrapper(planId: String, navController: NavController) {
    val viewModel: WorkoutViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    LaunchedEffect(planId) {
        viewModel.fetchPlanById(planId)
    }

    val plan = viewModel.currentPlan

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (plan == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy bài tập!")
        }
        return
    }

    WorkoutDetailScreen(
        plan = plan,
        onBack = { navController.popBackStack() },
        onComplete = {
            viewModel.completePlan(plan.id)
            navController.popBackStack()
        }
    )
}