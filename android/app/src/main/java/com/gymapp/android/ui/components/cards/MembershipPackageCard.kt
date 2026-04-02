package com.gymapp.android.ui.components.cards

import androidx.compose.foundation.BorderStroke
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
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.model.membership.PlanType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipPackageCard(
    plan: MembershipPlan,
    isFeatured: Boolean = false,
    isSelected: Boolean = false,
    onCardClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val formattedPrice = "${formatter.format(plan.price.toLong())}đ"

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF6F6F6)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFFFF5722)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isFeatured) {
                Text(
                    text = "Phổ biến nhất",
                    color = Color(0xFFFF5722),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            
            Text(
                text = "${plan.name} · ${plan.durationDays / 30} tháng",
                color = Color(0xFF1A1A1A),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$formattedPrice / ${plan.durationDays} ngày",
                color = Color(0xFFFF5722),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = if (plan.planType == PlanType.ALL) Color(0xFF1B5E20) else Color(0xFFE65100),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (plan.planType == PlanType.ALL) "Toàn chuỗi" else "1 Chi nhánh: ${plan.branchName ?: ""}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plan.description,
                color = Color(0xFF666666),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFFFF5722) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color(0xFF1A1A1A)
                ),
                border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Xem chi tiết →", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
