package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Shared helpers used across pt screens ─────────────────────────────────────
val AvatarBlueBg    = Color(0xFFB5D4F4)
val AvatarBlueText  = Color(0xFF042C53)
val AvatarTealBg    = Color(0xFF9FE1CB)
val AvatarTealText  = Color(0xFF04342C)

val AvatarColors = listOf(
    Pair(Color(0xFFB5D4F4), Color(0xFF042C53)),
    Pair(Color(0xFF9FE1CB), Color(0xFF04342C)),
    Pair(Color(0xFFFAC775), Color(0xFF412402)),
    Pair(Color(0xFFCECBF6), Color(0xFF26215C))
)

fun avatarInitials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) "${parts[0][0]}${parts.last()[0]}" else parts[0].take(2).uppercase()
}

fun formatDatetime(date: Date?): String {
    if (date == null) return ""
    val sdf = SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.getDefault())
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}

@Composable
fun AvatarBubble(
    name: String?, 
    modifier: Modifier = Modifier.size(28.dp),
    bgColor: Color = AvatarBlueBg, 
    textColor: Color = AvatarBlueText
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = avatarInitials(name),
            fontSize = (if (modifier == Modifier) 12.sp else 14.sp), // Default font size logic
            fontWeight = FontWeight.W500,
            color = textColor
        )
    }
}
