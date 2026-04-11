package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// ── Design Tokens ──────────────────────────────────────────────────────────────
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
private val AmberValue      = Color(0xFF633806)
private val RejectBg        = Color(0xFFFCEBEB)
private val RejectBorder    = Color(0xFFF7C1C1)
private val RejectText      = Color(0xFFA32D2D)
private val ConfirmedBg     = Color(0xFFEAF3DE)
private val ConfirmedBorder = Color(0xFFC0DD97)
private val ConfirmedText   = Color(0xFF3B6D11)

// ── Screen 5: PT Booking Queue ────────────────────────────────────────────────
@Composable
fun PtQueueScreen(
    viewModel: PtQueueViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadQueue()
    }

    val uiState by viewModel.uiState.collectAsState()

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Lịch hẹn của tôi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tprimary)
        }

        when (val state = uiState) {
            is PtQueueUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            is PtQueueUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadQueue() }) {
                            Text("Thử lại", color = GreenPrimary)
                        }
                    }
                }
            }
            is PtQueueUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    // ── Stat Grid ──────────────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val confirmedCount = state.confirmed.size
                            StatCard(label = "Đã xác nhận\ntháng này", value = "$confirmedCount", sub = "buổi", modifier = Modifier.weight(1f))
                        }
                    }

                    // ── Pending ────────────────────────────────────────────────
                    if (state.pending.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Chờ xác nhận",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Tprimary
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items(state.pending) { booking ->
                            PendingBookingCard(booking = booking)
                        }
                    }

                    // ── Confirmed ──────────────────────────────────────────────
                    if (state.confirmed.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Sắp tới (đã xác nhận)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Tprimary
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        items(state.confirmed) { booking ->
                            ConfirmedBookingRow(booking = booking)
                        }
                    }

                    if (state.pending.isEmpty() && state.confirmed.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có lịch hẹn nào", color = Tsecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BgSecondary, RoundedCornerShape(10.dp))
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Tsecondary, lineHeight = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
        Text(text = sub, fontSize = 14.sp, color = Tsecondary)
    }
}

@Composable
private fun PendingBookingCard(booking: BookingDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AmberBorder, RoundedCornerShape(8.dp))
            .background(BgPrimary, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AvatarBubble(name = booking.userName, modifier = Modifier.size(40.dp), bgColor = Color(0xFF9FE1CB), textColor = Color(0xFF04342C))
                Column {
                    Text(booking.userName ?: "User", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(formatDatetime(booking.scheduledAt), fontSize = 15.sp, color = Tsecondary)
                    Text("%,.0fđ · PT nhận: %,.0fđ".format(booking.totalAmount, booking.ptAmount ?: 0.0), fontSize = 15.sp, color = Tsecondary)
                }
            }
            Box(
                modifier = Modifier
                    .border(0.5.dp, AmberBorder, RoundedCornerShape(8.dp))
                    .background(AmberBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Chờ", fontSize = 12.sp, fontWeight = FontWeight.W500, color = AmberText)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Accept
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(0.5.dp, GreenBorder, RoundedCornerShape(8.dp))
                    .background(GreenLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenDark)
            }
            // Reject
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(0.5.dp, RejectBorder, RoundedCornerShape(8.dp))
                    .background(RejectBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Từ chối", fontSize = 16.sp, color = RejectText)
            }
        }
    }
}

@Composable
private fun ConfirmedBookingRow(booking: BookingDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.dp, color = Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            )
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AvatarBubble(name = booking.userName, modifier = Modifier.size(40.dp), bgColor = Color(0xFFB5D4F4), textColor = Color(0xFF042C53))
            Column {
                Text(booking.userName ?: "User", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                Text(formatDatetime(booking.scheduledAt), fontSize = 15.sp, color = Tsecondary)
            }
        }
        Box(
            modifier = Modifier
                .border(0.5.dp, ConfirmedBorder, RoundedCornerShape(8.dp))
                .background(ConfirmedBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Đã xác nhận", fontSize = 12.sp, fontWeight = FontWeight.W500, color = ConfirmedText)
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = BorderTertiary)
}
