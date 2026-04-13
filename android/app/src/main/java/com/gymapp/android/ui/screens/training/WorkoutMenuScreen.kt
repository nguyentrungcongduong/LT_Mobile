package com.gymapp.android.ui.screens.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WorkoutMenuScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Button(
            onClick = { navController.navigate("workout_list/USER_CUSTOM") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kế hoạch của tôi")
        }

        Button(
            onClick = { navController.navigate("workout_list/PT_ASSIGNED") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("PT giao")
        }

        Button(
            onClick = { navController.navigate("workout_list/RECOMMENDED") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gợi ý")
        }
    }
}