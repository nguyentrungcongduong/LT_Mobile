    package com.gymapp.android.ui.screens.training

    import android.util.Log
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.pager.HorizontalPager
    import androidx.compose.foundation.pager.rememberPagerState
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.History
    import androidx.compose.material.icons.filled.Person
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.FloatingActionButton
    import androidx.compose.material3.Icon
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.navigation.NavController
    import androidx.navigation.NavHostController
    import com.gymapp.android.R
    import com.gymapp.android.data.remote.response.WorkoutPlanResponse
    import com.gymapp.android.ui.screens.training.modal.PlanDetailModal
    import kotlinx.coroutines.delay

    @Composable
    fun WorkoutScreen(navController: NavHostController) {

        val viewModel: WorkoutViewModel = hiltViewModel()
        val plans = when (viewModel.selectedType) {
            "USER_CUSTOM" -> viewModel.customPlans
            "PT_ASSIGNED" -> viewModel.ptPlans
            else -> viewModel.recommendedPlans
        }
        var selectedPlan by remember { mutableStateOf<WorkoutPlanResponse?>(null) }

        LaunchedEffect(Unit) {
            viewModel.selectedType = "USER_CUSTOM"
            viewModel.loadByType("USER_CUSTOM")
            viewModel.loadWorkoutLogs()
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                val banners = listOf(
                    R.drawable.banner1,
                    R.drawable.banner2,
                    R.drawable.banner3
                )
                val pagerState = rememberPagerState(pageCount = { banners.size })
                val scope = rememberCoroutineScope()

    // Auto slide mỗi 2s
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(2000)
                        val next = (pagerState.currentPage + 1) % banners.size
                        pagerState.animateScrollToPage(next)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) { page ->
                    Image(
                        painter = painterResource(id = banners[page]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Column(modifier = Modifier.padding(horizontal = 16.dp)){
                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Chọn loại bài tập", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(modifier = Modifier.weight(1f), onClick = {
                            navController.navigate("create_schedule")
                        }) { Text("Tạo lịch") }

                        Button(modifier = Modifier.weight(1f), onClick = {
                            viewModel.loadByType("RECOMMENDED")
                        }) { Text("Gợi ý") }

                        Button(modifier = Modifier.weight(1f), onClick = {
                            viewModel.loadByType("PT_ASSIGNED")
                        }) { Text("PT giao") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (viewModel.isLoading) Text("Đang tải dữ liệu...")
                    if (!viewModel.isLoading && plans.isEmpty()) Text("Không có bài tập 😢")
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(plans) { plan  ->
                            val isCompleted = viewModel.completedPlanIds.contains(plan.id)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedPlan = plan },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCompleted)
                                        MaterialTheme.colorScheme.surfaceVariant
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {

                                    // 👉 NGÀY TẬP
                                    if (!plan.scheduledDate.isNullOrEmpty()) {
                                        Text(
                                            text = "📅 ${plan.scheduledDate}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Text(plan.name, fontWeight = FontWeight.Bold)
                                    Text(plan.description ?: "")
                                    plan.exercises.forEach { ex ->
                                        Text("• ${ex.exerciseName} - ${ex.sets}x${ex.reps}")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 👉 NÚT HOÀN THÀNH
                                    Button(
                                        onClick = {
                                            if (!isCompleted) viewModel.completePlan(plan.id)
                                        },
                                        enabled = !isCompleted,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCompleted)
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else
                                                MaterialTheme.colorScheme.primary,
                                            disabledContainerColor = Color.Gray
                                        )
                                    ) {
                                        Text(if (isCompleted) "✅ Đã hoàn thành" else "Hoàn thành")
                                    }
                                }

                            }
                        }
                    }
                }

            }

            // NÚT LỊCH SỬ - BOTTOM RIGHT
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.loadByType("USER_CUSTOM") }
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Của tôi")
                }

                FloatingActionButton(
                    onClick = { navController.navigate("workout_history") }
                ) {
                    Icon(Icons.Default.History, contentDescription = "Lịch sử tập luyện")
                }
            }
            selectedPlan?.let { plan ->
                PlanDetailModal(
                    plan = plan,
                    onDismiss = { selectedPlan = null }
                )
            }
        }
    }