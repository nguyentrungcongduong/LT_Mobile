package com.gymapp.android.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.model.membership.PlanType
import java.text.NumberFormat
import java.util.*

// ── Dark tokens ────────────────────────────────────────────────────────────────
private val CardBgDark     = Color(0xFF1C1C1E)
private val CardBorder     = Color(0xFF2A2A2E)
private val Tprimary       = Color(0xFFF2F2F2)
private val Tsecondary     = Color(0xFF9A9A9E)
private val Orange         = Color(0xFFFF6B2B)
private val OrangeGlow     = Color(0xFFFF8C00)
private val GreenTag       = Color(0xFF0D2B1E)
private val GreenTagBorder = Color(0xFF174D34)
private val GreenText      = Color(0xFF2ECC8E)
private val OrangeTag      = Color(0xFF2A1508)
private val OrangeTagBorder= Color(0xFF4A2510)

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
    val months = plan.durationDays / 30

    Card(
        onClick = onCardClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgDark),
        border = if (isSelected)
            BorderStroke(2.dp, Orange)
        else
            BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Featured badge ─────────────────────────────────────
            if (isFeatured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(OrangeGlow, Orange)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Text(
                            "Phổ biến nhất",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Plan name ──────────────────────────────────────────
            Text(
                text = "${plan.name} · $months tháng",
                color = Tprimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // ── Price ──────────────────────────────────────────────
            Text(
                text = "$formattedPrice / ${plan.durationDays} ngày",
                color = Orange,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Plan type badge ────────────────────────────────────
            val isAllChain = plan.planType == PlanType.ALL
            Row(
                modifier = Modifier
                    .background(
                        if (isAllChain) GreenTag else OrangeTag,
                        RoundedCornerShape(6.dp)
                    )
                    .then(
                        Modifier.run {
                            if (isAllChain)
                                this.padding(0.dp) // handled via background
                            else
                                this
                        }
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAllChain) "Toàn chuỗi" else "1 Chi nhánh: ${plan.branchName ?: ""}",
                    color = if (isAllChain) GreenText else Orange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Description ────────────────────────────────────────
            Text(
                text = plan.description,
                color = Tsecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── CTA Button ─────────────────────────────────────────
            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Orange else Color.Transparent,
                    contentColor   = if (isSelected) Color.White else Tprimary
                ),
                border = if (!isSelected) BorderStroke(1.dp, CardBorder) else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Xem chi tiết →",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
