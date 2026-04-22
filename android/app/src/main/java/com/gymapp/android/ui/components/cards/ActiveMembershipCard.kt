package com.gymapp.android.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.MembershipStatus
import com.gymapp.android.domain.model.membership.PlanType
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ActiveMembershipCard(membership: ActiveMembership) {
    val totalDays = ChronoUnit.DAYS.between(membership.startDate, membership.endDate).toInt()
    val daysUsed = totalDays - membership.daysLeft
    val progress = if (totalDays > 0) daysUsed.toFloat() / totalDays.toFloat() else 0f
    val fmt = DateTimeFormatter.ofPattern("dd/MM/yy")

    // Dynamic gradient based on status / remaining days
    val cardGradient = when {
        membership.status == MembershipStatus.EXPIRED ->
            listOf(Color(0xFF424242), Color(0xFF212121))
        membership.daysLeft <= 7 ->
            listOf(Color(0xFFB71C1C), Color(0xFF7F0000))
        membership.daysLeft <= 30 ->
            listOf(Color(0xFFE65100), Color(0xFFBF360C))
        else ->
            listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
    }

    val accentColor = when {
        membership.status == MembershipStatus.EXPIRED -> Color(0xFFBDBDBD)
        membership.daysLeft <= 7 -> Color(0xFFFF5252)
        membership.daysLeft <= 30 -> Color(0xFFFF8C00)
        else -> Color(0xFFFF8C00)
    }

    val progressBarColor = when {
        progress >= 0.9f -> Color(0xFFFF5252)
        progress >= 0.7f -> Color(0xFFFF8C00)
        else -> Color(0xFF4FC3F7)
    }

    // ── Premium ATM-style Card ────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = cardGradient,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(22.dp)
    ) {
        // Decorative circle top-right
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0x0AFFFFFF))
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0x08FFFFFF))
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = 40.dp)
        )

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Top row: Badge + label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            "HỘI VIÊN",
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        membership.planName.uppercase(),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Status pill
                val (statusText, statusColor) = when (membership.status) {
                    MembershipStatus.ACTIVE -> "ACTIVE" to Color(0xFF69F0AE)
                    MembershipStatus.PENDING -> "ĐANG XỬ LÝ" to Color(0xFFFFCC02)
                    MembershipStatus.EXPIRED -> "HẾT HẠN" to Color(0xFFFF5252)
                    MembershipStatus.FROZEN -> "TẠM DỪNG" to Color(0xFF4FC3F7)
                    MembershipStatus.CANCELLED -> "ĐÃ HỦY" to Color(0xFFBDBDBD)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Branch
                Text(
                    "${if (membership.planType == PlanType.ALL) "Tất cả chi nhánh" else membership.branchName}",
                    color = Color(0x99FFFFFF),
                    fontSize = 11.sp
                )

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(accentColor, progressBarColor))
                            )
                    )
                }

                // Dates row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bắt đầu", color = Color(0x77FFFFFF), fontSize = 10.sp)
                        Text(
                            membership.startDate.format(fmt),
                            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Đã dùng", color = Color(0x77FFFFFF), fontSize = 10.sp)
                        Text(
                            "${daysUsed}d",
                            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Còn lại", color = Color(0x77FFFFFF), fontSize = 10.sp)
                        Text(
                            "${membership.daysLeft} ngày",
                            color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
