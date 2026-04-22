package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Dark Design Tokens ─────────────────────────────────────────────────────────
private val BgPrimary       = Color(0xFF121212)
private val BgSecondary     = Color(0xFF1C1C1E)
private val BgCard          = Color(0xFF1E1E22)
private val BorderDark      = Color(0xFF2A2A2E)
private val Tprimary        = Color(0xFFF2F2F2)
private val Tsecondary      = Color(0xFF9A9A9E)
private val OrangePrimary   = Color(0xFFFF6B2B)
private val OrangeGlow      = Color(0xFFFF8C00)

// Status colors (dark-mode adapted)
private val GreenText       = Color(0xFF2ECC8E)
private val GreenBg         = Color(0xFF0D2B1E)
private val GreenBorder     = Color(0xFF174D34)

private val AmberText       = Color(0xFFFFB300)
private val AmberBg         = Color(0xFF2A1F00)
private val AmberBorder     = Color(0xFF4A3800)

private val RejectText      = Color(0xFFEF5350)
private val RejectBg        = Color(0xFF2A0E0E)
private val RejectBorder    = Color(0xFF4A1515)

private val DoneText        = Color(0xFF9A9A9E)
private val DoneBg          = Color(0xFF252528)
private val DoneBorder      = Color(0xFF3A3A3E)

data class StatusInfo(
    val label: String,
    val icon: ImageVector,
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color
)

private fun getStatusInfo(status: String): StatusInfo = when (status) {
    "CONFIRMED" -> StatusInfo("Đã xác nhận", Icons.Default.CheckCircle,  GreenText, GreenBg, GreenBorder)
    "PENDING"   -> StatusInfo("Chờ thanh toán", Icons.Default.HourglassEmpty, AmberText, AmberBg, AmberBorder)
    "CANCELLED" -> StatusInfo("Đã hủy", Icons.Default.Cancel, RejectText, RejectBg, RejectBorder)
    "COMPLETED" -> StatusInfo("Hoàn thành", Icons.Default.EmojiEvents, DoneText, DoneBg, DoneBorder)
    else        -> StatusInfo(status, Icons.Default.CalendarMonth, Tsecondary, BgSecondary, BorderDark)
}

// ── Screen ─────────────────────────────────────────────────────────────────────
@Composable
fun UserBookingsScreen(
    onNavigateToCancel: (String) -> Unit = {},
    viewModel: UserBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabs = listOf("Sắp tới", "Đã xong", "Đã hủy")

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadBookings()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // ── Header with gradient ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF1C1C1E), Color(0xFF252528)))
                )
                .border(width = 0.5.dp, color = BorderDark)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Lịch hẹn của tôi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tprimary
                )
            }
        }

        // ── Custom Tab Row ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .border(width = 0.5.dp, color = BorderDark)
        ) {
            tabs.forEachIndexed { index, label ->
                val isActive = selectedTab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(index) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 15.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) OrangePrimary else Tsecondary
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(OrangeGlow, OrangePrimary)),
                                    RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────
        when (val state = uiState) {
            is UserBookingsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
            is UserBookingsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadBookings() }) {
                            Text("Thử lại", color = OrangePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is UserBookingsUiState.Success -> {
                if (state.bookings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(BgSecondary, RoundedCornerShape(50.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Tsecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Text(
                                "Không có lịch hẹn nào",
                                color = Tprimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Đặt lịch PT để bắt đầu hành trình tập luyện!",
                                color = Tsecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.bookings) { booking ->
                            UserBookingCard(
                                booking = booking,
                                onClick = {
                                    if (booking.status == "CONFIRMED") {
                                        val encodedName = java.net.URLEncoder.encode(booking.ptName ?: "PT", "UTF-8")
                                        val timestamp = booking.scheduledAt?.toInstant()?.toEpochMilli() ?: 0L
                                        onNavigateToCancel("cancel_booking/${booking.id}/$encodedName/$timestamp/${booking.totalAmount}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserBookingCard(booking: BookingDto, onClick: () -> Unit) {
    val info = getStatusInfo(booking.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(1.dp, BorderDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AvatarBubble(name = booking.ptName, modifier = Modifier.size(48.dp))

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = booking.ptName ?: "PT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tprimary
                )
                Text(
                    text = formatDatetime(booking.scheduledAt),
                    fontSize = 13.sp,
                    color = Tsecondary
                )
                Text(
                    text = "%,.0fđ".format(booking.totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangePrimary
                )
            }

            // Status badge
            Column(
                modifier = Modifier
                    .background(info.bgColor, RoundedCornerShape(10.dp))
                    .border(1.dp, info.borderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    info.icon,
                    contentDescription = null,
                    tint = info.textColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = info.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = info.textColor
                )
            }
        }
    }
}
