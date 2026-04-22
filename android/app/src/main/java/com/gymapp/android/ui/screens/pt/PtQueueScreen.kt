package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.BookingDto

// ── Dark Design Tokens ─────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFF121212)
private val BgSecondary    = Color(0xFF1C1C1E)
private val BgCard         = Color(0xFF1E1E22)
private val BorderDark     = Color(0xFF2A2A2E)
private val Tprimary       = Color(0xFFF2F2F2)
private val Tsecondary     = Color(0xFF9A9A9E)
private val Orange         = Color(0xFFFF6B2B)
private val OrangeGlow     = Color(0xFFFF8C00)
private val OrangeDim      = Color(0xFF2A1508)
private val GreenText      = Color(0xFF2ECC8E)
private val GreenBg        = Color(0xFF0D2B1E)
private val AmberText      = Color(0xFFF0B429)
private val AmberBg        = Color(0xFF281A00)
private val RedText        = Color(0xFFEF5350)
private val RedBg          = Color(0xFF2A0A0A)
private val PurpleText     = Color(0xFFB39DDB)  // màu cho AWAITING_CONFIRMATION
private val PurpleBg       = Color(0xFF1A1028)

// ── Screen: PT Booking Queue ──────────────────────────────────────────────────
@Composable
fun PtQueueScreen(
    viewModel: PtQueueViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadQueue() }

    val uiState by viewModel.uiState.collectAsState()
    val attendanceState by viewModel.attendanceState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiện Snackbar khi xác nhận xong
    LaunchedEffect(attendanceState) {
        when (val s = attendanceState) {
            is AttendanceState.Success -> {
                snackbarHostState.showSnackbar(s.message)
                viewModel.resetAttendanceState()
            }
            is AttendanceState.Error -> {
                snackbarHostState.showSnackbar("❌ ${s.message}")
                viewModel.resetAttendanceState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BgPrimary
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BgPrimary)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(BgSecondary, BgPrimary)))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(OrangeDim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Lịch hẹn của tôi", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Tprimary)
                        Text("Quản lý yêu cầu và buổi dạy", fontSize = 12.sp, color = Tsecondary)
                    }
                }
            }

            // ── Content ────────────────────────────────────────────────────────
            when (val state = uiState) {
                is PtQueueUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }
                is PtQueueUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(state.message, color = Tsecondary, fontSize = 14.sp)
                            TextButton(onClick = { viewModel.loadQueue() }) {
                                Text("Thử lại", color = Orange)
                            }
                        }
                    }
                }
                is PtQueueUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
                    ) {
                        // ── Stat Cards ──────────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DarkStatCard(
                                    label = "Đã xác nhận",
                                    value = "${state.confirmed.size}",
                                    sub = "buổi",
                                    accentColor = GreenText,
                                    bgColor = GreenBg,
                                    modifier = Modifier.weight(1f)
                                )
                                DarkStatCard(
                                    label = "Chờ thanh toán",
                                    value = "${state.pending.size}",
                                    sub = "yêu cầu",
                                    accentColor = AmberText,
                                    bgColor = AmberBg,
                                    modifier = Modifier.weight(1f)
                                )
                                if (state.awaitingConfirmation.isNotEmpty()) {
                                    DarkStatCard(
                                        label = "Cần xác nhận",
                                        value = "${state.awaitingConfirmation.size}",
                                        sub = "buổi",
                                        accentColor = PurpleText,
                                        bgColor = PurpleBg,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // ── AWAITING_CONFIRMATION: PT bấm xác nhận ────────────
                        if (state.awaitingConfirmation.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(Modifier.size(6.dp).background(PurpleText, CircleShape))
                                    Text("Cần xác nhận buổi tập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                                }
                            }
                            items(state.awaitingConfirmation) { booking ->
                                AwaitingConfirmationCard(
                                    booking = booking,
                                    isLoading = attendanceState is AttendanceState.Loading,
                                    onAttended = { viewModel.confirmAttendance(booking.id, true) },
                                    onNoShow   = { viewModel.confirmAttendance(booking.id, false) }
                                )
                            }
                        }

                        // ── PENDING ─────────────────────────────────────────────
                        if (state.pending.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(Modifier.size(6.dp).background(AmberText, CircleShape))
                                    Text("Chờ xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                                }
                            }
                            items(state.pending) { booking ->
                                PendingBookingCard(booking = booking)
                            }
                        }

                        // ── CONFIRMED ───────────────────────────────────────────
                        if (state.confirmed.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(Modifier.size(6.dp).background(GreenText, CircleShape))
                                    Text("Sắp tới · Đã xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                                }
                            }
                            items(state.confirmed) { booking ->
                                ConfirmedBookingRow(booking = booking)
                            }
                        }

                        // ── Empty State ─────────────────────────────────────────
                        if (state.pending.isEmpty() && state.confirmed.isEmpty() && state.awaitingConfirmation.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxWidth().padding(top = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(80.dp).background(BgSecondary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.CalendarMonth, null, tint = Tsecondary, modifier = Modifier.size(36.dp))
                                        }
                                        Text("Chưa có lịch hẹn nào", color = Tprimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Học viên đặt lịch sẽ xuất hiện ở đây", color = Tsecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Awaiting Confirmation Card ─────────────────────────────────────────────────
// Hiện sau khi buổi tập đã kết thúc — PT xác nhận học viên có tham gia không
@Composable
private fun AwaitingConfirmationCard(
    booking: BookingDto,
    isLoading: Boolean,
    onAttended: () -> Unit,
    onNoShow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(16.dp))
            .border(1.dp, PurpleText.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AvatarBubble(
                    name = booking.userName,
                    modifier = Modifier.size(44.dp),
                    bgColor = Color(0xFF221A3A),
                    textColor = PurpleText
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(booking.userName ?: "User", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(formatDatetime(booking.scheduledAt), fontSize = 13.sp, color = Tsecondary)
                    Text("Buổi tập đã kết thúc", fontSize = 12.sp, color = PurpleText)
                }
            }
            Box(
                modifier = Modifier
                    .background(PurpleBg, RoundedCornerShape(10.dp))
                    .border(1.dp, PurpleText.copy(0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("Cần xác nhận", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PurpleText)
            }
        }

        HorizontalDivider(color = BorderDark, thickness = 1.dp)

        Text(
            "Học viên có đến tập buổi này không?",
            fontSize = 13.sp,
            color = Tsecondary,
            fontWeight = FontWeight.Medium
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ Đã tập
            Button(
                onClick = onAttended,
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenText.copy(0.5f))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GreenText, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, null, tint = GreenText, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Đã tập", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenText)
                }
            }
            // ❌ Vắng mặt
            Button(
                onClick = onNoShow,
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RedText.copy(0.5f))
            ) {
                Icon(Icons.Default.Close, null, tint = RedText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Vắng mặt", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RedText)
            }
        }
    }
}

// ── Dark Stat Card ─────────────────────────────────────────────────────────────
@Composable
private fun DarkStatCard(
    label: String,
    value: String,
    sub: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Tsecondary, lineHeight = 17.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
        Text(sub, fontSize = 12.sp, color = Tsecondary)
    }
}

// ── Pending Booking Card ───────────────────────────────────────────────────────
@Composable
private fun PendingBookingCard(booking: BookingDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(16.dp))
            .border(1.dp, AmberText.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AvatarBubble(
                    name = booking.userName,
                    modifier = Modifier.size(44.dp),
                    bgColor = Color(0xFF3D2A00),
                    textColor = AmberText
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(booking.userName ?: "User", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(formatDatetime(booking.scheduledAt), fontSize = 13.sp, color = Tsecondary)
                    Text(
                        "%,.0fđ · PT: %,.0fđ".format(booking.totalAmount, booking.ptAmount ?: 0.0),
                        fontSize = 12.sp,
                        color = AmberText
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(AmberBg, RoundedCornerShape(10.dp))
                    .border(1.dp, AmberText.copy(0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.HourglassTop, null, tint = AmberText, modifier = Modifier.size(12.dp))
                    Text("Chờ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AmberText)
                }
            }
        }
    }
}

// ── Confirmed Booking Row ──────────────────────────────────────────────────────
@Composable
private fun ConfirmedBookingRow(booking: BookingDto) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AvatarBubble(
                    name = booking.userName,
                    modifier = Modifier.size(44.dp),
                    bgColor = Color(0xFF0D1F2D),
                    textColor = Color(0xFF5BA8D4)
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(booking.userName ?: "User", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(formatDatetime(booking.scheduledAt), fontSize = 13.sp, color = Tsecondary)
                }
            }
            Box(
                modifier = Modifier
                    .background(GreenBg, RoundedCornerShape(10.dp))
                    .border(1.dp, GreenText.copy(0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text("Đã xác nhận", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenText)
            }
        }
        HorizontalDivider(color = BorderDark, thickness = 1.dp)
    }
}


// ── Format datetime ────────────────────────────────────────────────────────────
private fun formatDatetime(dt: String?): String {
    if (dt == null) return ""
    return try {
        val parser = java.time.OffsetDateTime.parse(dt)
        parser.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    } catch (e: Exception) { dt.take(16).replace("T", " ") }
}
