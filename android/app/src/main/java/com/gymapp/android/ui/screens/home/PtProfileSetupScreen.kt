package com.gymapp.android.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal

// ── Màu sắc UI chuyên nghiệp cho PT ──────────────────────────────────────────
private val PtDark      = Color(0xFF0F172A)   // nền tối chuyên nghiệp
private val PtMidDark   = Color(0xFF1E293B)
private val PtAccent    = Color(0xFF6366F1)   // tím indigo
private val PtAccentSec = Color(0xFF8B5CF6)   // tím nhạt hơn
private val PtGold      = Color(0xFFF59E0B)   // vàng nhấn
private val PtGreen     = Color(0xFF10B981)   // xanh lá
private val PtCard      = Color(0xFF1E293B)
private val PtBorder    = Color(0xFF334155)
private val PtText      = Color(0xFFF1F5F9)
private val PtSubText   = Color(0xFF94A3B8)

data class PtSpecialty(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    /** map sang FitnessGoal / ExperienceLevel tương ứng gửi API */
    val mappedGoal: FitnessGoal,
    val mappedLevel: ExperienceLevel
)

private val ptSpecialties = listOf(
    PtSpecialty(
        id = "strength",
        title = "Sức mạnh & Tăng cơ",
        subtitle = "Hypertrophy, powerlifting, strength training",
        icon = Icons.Default.FitnessCenter,
        accentColor = PtAccent,
        mappedGoal = FitnessGoal.MUSCLE_GAIN,
        mappedLevel = ExperienceLevel.ADVANCED
    ),
    PtSpecialty(
        id = "cardio",
        title = "Cardio & Giảm mỡ",
        subtitle = "HIIT, fat loss, endurance coaching",
        icon = Icons.Default.DirectionsRun,
        accentColor = PtGold,
        mappedGoal = FitnessGoal.WEIGHT_LOSS,
        mappedLevel = ExperienceLevel.INTERMEDIATE
    ),
    PtSpecialty(
        id = "endurance",
        title = "Sức bền & Thể lực",
        subtitle = "Functional training, sports conditioning",
        icon = Icons.Default.SportsScore,
        accentColor = PtGreen,
        mappedGoal = FitnessGoal.ENDURANCE,
        mappedLevel = ExperienceLevel.ADVANCED
    ),
)

private val ptYearsOptions = listOf(
    Triple("1-2 năm", "Mới vào nghề, đang tích lũy kinh nghiệm", ExperienceLevel.BEGINNER),
    Triple("3-5 năm", "Có kinh nghiệm thực tế, đã có khách hàng", ExperienceLevel.INTERMEDIATE),
    Triple("5+ năm", "Chuyên nghiệp, huấn luyện chuyên sâu", ExperienceLevel.ADVANCED),
)

// ─────────────────────────────────────────────────────────────────────────────
// PT Profile Setup Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PtProfileSetupScreen(
    onDone: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var selectedSpecialty by remember { mutableStateOf(ptSpecialties[0]) }
    var selectedYearIndex by remember { mutableStateOf(1) }

    // Đồng bộ lựa chọn vào ViewModel để gọi API submit
    LaunchedEffect(selectedSpecialty, selectedYearIndex) {
        viewModel.fitnessGoal = selectedSpecialty.mappedGoal
        viewModel.experienceLevel = ptYearsOptions[selectedYearIndex].third
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PtDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Header gradient ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PtAccent.copy(alpha = 0.35f),
                                PtDark
                            )
                        )
                    )
                    .padding(top = 56.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    // Badge
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(PtAccent, PtAccentSec)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "PERSONAL TRAINER",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Thiết lập\nhồ sơ HLV",
                        color = PtText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 38.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Giúp học viên tìm đúng HLV phù hợp với họ",
                        color = PtSubText,
                        fontSize = 14.sp
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Section 1: Chuyên môn
                PtSection(title = "Chuyên môn chính", stepNumber = "01") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ptSpecialties.forEach { specialty ->
                            PtSpecialtyCard(
                                specialty = specialty,
                                selected = selectedSpecialty.id == specialty.id,
                                onClick = { selectedSpecialty = specialty }
                            )
                        }
                    }
                }

                // Section 2: Số năm kinh nghiệm
                PtSection(title = "Số năm kinh nghiệm", stepNumber = "02") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ptYearsOptions.forEachIndexed { index, (years, _, _) ->
                            PtYearChip(
                                label = years,
                                selected = selectedYearIndex == index,
                                accentColor = when (index) {
                                    0 -> PtGreen
                                    1 -> PtAccent
                                    else -> PtGold
                                },
                                onClick = { selectedYearIndex = index },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        ptYearsOptions[selectedYearIndex].second,
                        color = PtSubText,
                        fontSize = 13.sp
                    )
                }

                // Error message
                viewModel.errorMessage?.let {
                    Text(it, color = Color(0xFFEF4444), fontSize = 14.sp)
                }

                // ── Submit button ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(PtAccent, PtAccentSec)))
                        .clickable(enabled = !viewModel.isLoading) { viewModel.submit(onDone) }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Hoàn tất hồ sơ",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PtSection(
    title: String,
    stepNumber: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Step number badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(PtAccent.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, PtAccent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(stepNumber, color = PtAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = PtText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
private fun PtSpecialtyCard(
    specialty: PtSpecialty,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) specialty.accentColor.copy(alpha = 0.12f) else PtCard,
        animationSpec = tween(250), label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) specialty.accentColor else PtBorder,
        animationSpec = tween(250), label = "border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    if (selected) specialty.accentColor.copy(alpha = 0.25f)
                    else Color(0xFF0F172A),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                specialty.icon,
                contentDescription = null,
                tint = if (selected) specialty.accentColor else PtSubText,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                specialty.title,
                color = if (selected) PtText else PtSubText,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(Modifier.height(3.dp))
            Text(specialty.subtitle, color = PtSubText, fontSize = 12.sp, lineHeight = 17.sp)
        }

        if (selected) {
            Spacer(Modifier.width(10.dp))
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = specialty.accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PtYearChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        if (selected) accentColor.copy(alpha = 0.18f) else PtCard, tween(200), label = "chip"
    )
    val borderColor by animateColorAsState(
        if (selected) accentColor else PtBorder, tween(200), label = "chipBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) accentColor else PtSubText,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
