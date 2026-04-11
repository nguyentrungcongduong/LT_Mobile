package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ── Design Tokens (từ wireframe-spec.md) ──────────────────────────────────────
private val BgPrimary       = Color(0xFFFFFFFF)
private val BgSecondary     = Color(0xFFF5F5F5)
private val BorderTertiary  = Color(0xFFEBEBEB)
private val Tprimary        = Color(0xFF1A1A1A)
private val Tsecondary      = Color(0xFF6B6B6B)
private val GreenPrimary    = Color(0xFF1D9E75)
private val GreenLight      = Color(0xFFE1F5EE)
private val GreenBorder     = Color(0xFF9FE1CB)
private val GreenDark       = Color(0xFF085041)
private val AmberBg         = Color(0xFFFAEEDA)
private val AmberBorder     = Color(0xFFFAC775)
private val AmberText       = Color(0xFF854F0B)
private val RejectBg        = Color(0xFFFCEBEB)
private val RejectBorder    = Color(0xFFF7C1C1)
private val RejectText      = Color(0xFFA32D2D)
private val ConfirmedBg     = Color(0xFFEAF3DE)
private val ConfirmedBorder = Color(0xFFC0DD97)
private val ConfirmedText   = Color(0xFF3B6D11)



// ── Reusable: Status Badge ─────────────────────────────────────────────────────
@Composable
private fun StatusBadge(status: String) {
    val (bg, border, text, label) = when (status) {
        "CONFIRMED" -> listOf(ConfirmedBg, ConfirmedBorder, ConfirmedText, "Đã xác nhận")
        "PENDING"   -> listOf(AmberBg, AmberBorder, AmberText, "Chờ")
        "CANCELLED" -> listOf(RejectBg, RejectBorder, RejectText, "Đã hủy")
        "COMPLETED" -> listOf(Color(0xFFF1EFE8), Color(0xFFD3D1C7), Color(0xFF5F5E5A), "Hoàn thành")
        else        -> listOf(BgSecondary, BorderTertiary, Tsecondary, status)
    }
    @Suppress("UNCHECKED_CAST")
    (bg as? Color)?.let { bgColor ->
        Box(
            modifier = Modifier
                .border(0.5.dp, border as Color, RoundedCornerShape(8.dp))
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = label as String, fontSize = 12.sp, fontWeight = FontWeight.W500, color = text as Color)
        }
    }
}

// ── Screen 3: My Bookings (User) ───────────────────────────────────────────────
@Composable
fun UserBookingsScreen(
    onNavigateToCancel: (String) -> Unit = {},
    viewModel: UserBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabs = listOf("Sắp tới", "Đã xong", "Đã hủy")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .border(0.5.dp, BorderTertiary)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lịch hẹn của tôi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Tprimary
            )
        }

        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 0.5.dp,
                    color = BorderTertiary,
                    shape = RoundedCornerShape(0.dp)
                )
        ) {
            val blueActive = Color(0xFF185FA5)
            tabs.forEachIndexed { index, label ->
                val isActive = selectedTab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(index) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) blueActive else Tsecondary
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(1.5.dp)
                                .background(blueActive)
                        )
                    }
                }
            }
        }

        // Content
        when (val state = uiState) {
            is UserBookingsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF185FA5))
                }
            }
            is UserBookingsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadBookings() }) {
                            Text("Thử lại", color = Color(0xFF185FA5))
                        }
                    }
                }
            }
            is UserBookingsUiState.Success -> {
                if (state.bookings.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Không có lịch hẹn nào", color = Tsecondary, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(8.dp))
            .background(BgPrimary, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBubble(name = booking.ptName, modifier = Modifier.size(40.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = booking.ptName ?: "PT",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Tprimary
            )
            Text(
                text = formatDatetime(booking.scheduledAt),
                fontSize = 15.sp,
                color = Tsecondary
            )
            Text(
                text = "%,.0fđ".format(booking.totalAmount),
                fontSize = 15.sp,
                color = Tsecondary
            )
        }
        StatusBadge(status = booking.status)
    }
}
