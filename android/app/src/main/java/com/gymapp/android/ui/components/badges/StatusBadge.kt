package com.gymapp.android.ui.components.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymapp.android.domain.model.membership.MembershipStatus

@Composable
fun StatusBadge(status: MembershipStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, borderColor) = when (status) {
        MembershipStatus.ACTIVE -> Triple(Color(0xFFEAF3DE), Color(0xFF3B6D11), Color(0xFFC0DD97))
        MembershipStatus.EXPIRED -> Triple(Color(0xFFFCEBEB), Color(0xFFA32D2D), Color(0xFFF7C1C1))
        MembershipStatus.FROZEN -> Triple(Color(0xFFF1EFE8), Color(0xFF5F5E5A), Color(0xFFD3D1C7))
        MembershipStatus.PENDING -> Triple(Color(0xFFFAEEDA), Color(0xFF854F0B), Color(0xFFFAC775))
        MembershipStatus.CANCELLED -> Triple(Color(0xFFFCEBEB), Color(0xFFA32D2D), Color(0xFFF7C1C1))
    }

    val statusText = when (status) {
        MembershipStatus.ACTIVE -> "ACTIVE"
        MembershipStatus.EXPIRED -> "HẾT HẠN"
        MembershipStatus.FROZEN -> "TẠM DỪNG"
        MembershipStatus.PENDING -> "ĐANG XỬ LÝ"
        MembershipStatus.CANCELLED -> "ĐÃ HỦY"
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(4.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
