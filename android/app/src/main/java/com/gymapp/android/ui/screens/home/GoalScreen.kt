package com.gymapp.android.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal

// Colors from the app theme
private val BgColor = Color(0xFFF8F9FA)
private val CardBackground = Color.White
private val PrimaryOrange = Color(0xFFFF5722)
private val TextDark = Color(0xFF111827)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    onDone: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thiết lập mục tiêu", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    titleContentColor = TextDark
                )
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            
            // Experience Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Kinh nghiệm tập luyện",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                
                ExperienceLevel.entries.forEach { level ->
                    SelectableCard(
                        selected = viewModel.experienceLevel == level,
                        onClick = { viewModel.experienceLevel = level },
                        title = getExperienceTitle(level),
                        description = getExperienceDescription(level),
                        icon = getExperienceIcon(level)
                    )
                }
            }

            // Goal Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Mục tiêu chính",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                FitnessGoal.entries.forEach { goal ->
                    SelectableCard(
                        selected = viewModel.fitnessGoal == goal,
                        onClick = { viewModel.fitnessGoal = goal },
                        title = getGoalTitle(goal),
                        description = getGoalDescription(goal),
                        icon = getGoalIcon(goal)
                    )
                }
            }

            // Error Message
            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Extra space to push the button down if the screen is large
            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Submit Button
            Button(
                onClick = { viewModel.submit(onDone) },
                enabled = !viewModel.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Lưu mục tiêu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String,
    icon: ImageVector
) {
    val borderColor = if (selected) PrimaryOrange else Color(0xFFE5E7EB)
    val backgroundColor = if (selected) Color(0xFFFFF0EC) else CardBackground
    val iconTint = if (selected) PrimaryOrange else TextGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (selected) PrimaryOrange.copy(alpha = 0.2f) else Color(0xFFF3F4F6),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp
            )
        }
    }
}

private fun getExperienceTitle(level: ExperienceLevel): String = when(level) {
    ExperienceLevel.BEGINNER -> "Người mới"
    ExperienceLevel.INTERMEDIATE -> "Đã có kinh nghiệm"
    ExperienceLevel.ADVANCED -> "Nâng cao"
}

private fun getExperienceDescription(level: ExperienceLevel): String = when(level) {
    ExperienceLevel.BEGINNER -> "Chưa từng tập hoặc mới bắt đầu"
    ExperienceLevel.INTERMEDIATE -> "Đã tập luyện từ 6-12 tháng"
    ExperienceLevel.ADVANCED -> "Lịch tập chuyên nghiệp, lâu năm"
}

private fun getGoalTitle(goal: FitnessGoal): String = when(goal) {
    FitnessGoal.WEIGHT_LOSS -> "Giảm cân & Mỡ"
    FitnessGoal.MUSCLE_GAIN -> "Tăng cơ bắp"
    FitnessGoal.ENDURANCE -> "Tăng thể lực & Sức bền"
}

private fun getGoalDescription(goal: FitnessGoal): String = when(goal) {
    FitnessGoal.WEIGHT_LOSS -> "Đốt mỡ thừa, làm săn chắc cơ thể"
    FitnessGoal.MUSCLE_GAIN -> "Xây dựng khối lượng cơ, tăng cân"
    FitnessGoal.ENDURANCE -> "Cải thiện sức khỏe tim mạch, độ bền"
}

private fun getExperienceIcon(level: ExperienceLevel): ImageVector = when(level) {
    ExperienceLevel.BEGINNER -> Icons.Default.AccessibilityNew
    ExperienceLevel.INTERMEDIATE -> Icons.Default.FitnessCenter
    ExperienceLevel.ADVANCED -> Icons.Default.StarRate
}

private fun getGoalIcon(goal: FitnessGoal): ImageVector = when(goal) {
    FitnessGoal.WEIGHT_LOSS -> Icons.Default.DirectionsRun
    FitnessGoal.MUSCLE_GAIN -> Icons.Default.FitnessCenter
    FitnessGoal.ENDURANCE -> Icons.Default.AccessibilityNew
}