package com.gymapp.android.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.PlanType
import com.gymapp.android.ui.components.badges.StatusBadge
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ActiveMembershipCard(
    membership: ActiveMembership
) {
    val totalDays = ChronoUnit.DAYS.between(membership.startDate, membership.endDate).toInt()
    val daysUsed = totalDays - membership.daysLeft
    val progress = if (totalDays > 0) daysUsed.toFloat() / totalDays.toFloat() else 0f
    
    val progressColor = when {
        progress >= 0.9f -> Color(0xFFA32D2D) // Đỏ
        progress >= 0.7f -> Color(0xFFBA7517) // Cam
        else -> Color(0xFF185FA5) // Xanh dương
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = membership.planName,
                        color = Color(0xFF1A1A1A),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chi nhánh: ${if (membership.planType == PlanType.ALL) "Tất cả" else membership.branchName}",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                }
                StatusBadge(status = membership.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFE0E0E0))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Bắt đầu", color = Color(0xFF666666), fontSize = 12.sp)
                    Text(
                        text = membership.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Hết hạn", color = Color(0xFF666666), fontSize = 12.sp)
                    Text(
                        text = membership.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Đã dùng $daysUsed ngày",
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
                Text(
                    text = "Còn ${membership.daysLeft} ngày",
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
