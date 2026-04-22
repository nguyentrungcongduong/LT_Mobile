package com.gymapp.android.ui.screens.pt

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gymapp.android.data.remote.api.PtReviewDto
import com.gymapp.android.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val Orange = Color(0xFFFF5722)
private val OrangeLight = Color(0xFFFFF0EC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtBookingScreen(
    onNavigateBack: () -> Unit,
    onNext: (String) -> Unit,
    viewModel: PtBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ptDetail by viewModel.ptDetail.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSlotIds by viewModel.selectedSlotIds.collectAsState()
    val viewingMonth by viewModel.viewingMonth.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    val today = Calendar.getInstance()

    // Tính tổng tiền dựa trên số slot đã chọn và giá PT
    val pricePerSlot = ptDetail?.price ?: 0.0
    val totalAmount = (pricePerSlot * selectedSlotIds.size).toLong()

    // Handle booking success → navigate
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PtBookingUiState.BatchBookingSuccess -> {
                val url = state.response.paymentUrl ?: ""
                onNext(viewModel.ptId)
            }
            is PtBookingUiState.BookingSuccess -> onNext(viewModel.ptId)
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Đặt lịch PT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Trở về",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column {
                // ── Summary Bar ─────────────────────────────────────────────
                AnimatedVisibility(
                    visible = selectedSlotIds.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Orange.copy(alpha = 0.08f))
                            .border(1.dp, Orange.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Orange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${selectedSlotIds.size}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        "buổi đã chọn",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "Có thể chọn thêm từ ngày khác",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    formatVnd(totalAmount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Orange
                                )
                                Text(
                                    "Tổng cộng",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }

                // ── Action Button ────────────────────────────────────────────
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val isEnabled = selectedSlotIds.isNotEmpty() && selectedProvider != null
                        Button(
                            onClick = { viewModel.confirmBatchBooking() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            enabled = isEnabled && uiState !is PtBookingUiState.Loading
                        ) {
                            if (uiState is PtBookingUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                val label = if (selectedSlotIds.isEmpty()) "Chọn ít nhất 1 buổi"
                                else "Tiếp theo: Xác nhận ${selectedSlotIds.size} buổi"
                                Text(
                                    label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isEnabled) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── PT Info Strip ─────────────────────────────────────────────
            ptDetail?.let { pt ->
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    if (pt.avatarUrl.isNullOrBlank()) {
                        AvatarBubble(name = pt.fullName, modifier = Modifier.size(56.dp))
                    } else {
                        AsyncImage(
                            model = pt.avatarUrl,
                            contentDescription = pt.fullName,
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            pt.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            pt.specializations?.joinToString(" · ") ?: "Personal Trainer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${pt.rating} (${pt.reviewCount})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (pricePerSlot > 0) {
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${formatVnd(pricePerSlot.toLong())}/buổi",
                                    fontSize = 13.sp,
                                    color = Orange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Calendar Section ──────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        monthFormat.format(viewingMonth.time).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row {
                        IconButton(onClick = { viewModel.prevMonth() }) {
                            Text("<", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Text(">", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                        Text(
                            day, modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Calendar grid
                val calendarArrangement = remember(viewingMonth) {
                    val cal = viewingMonth.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val list = mutableListOf<Date?>()
                    repeat(firstDayOfWeek) { list.add(null) }
                    for (i in 1..daysInMonth) {
                        cal.set(Calendar.DAY_OF_MONTH, i)
                        list.add(cal.time)
                    }
                    list
                }

                // Dates that have available slots (for dots indicator)
                val datesWithSlots: Set<String> = remember(uiState) {
                    if (uiState is PtBookingUiState.Success) {
                        (uiState as PtBookingUiState.Success).availabilities
                            .filter { !it.isBooked }
                            .map { it.availableDate }
                            .toSet()
                    } else emptySet()
                }

                // Dates that have selected slots
                val datesWithSelectedSlots: Set<String> = remember(uiState, selectedSlotIds) {
                    if (uiState is PtBookingUiState.Success) {
                        (uiState as PtBookingUiState.Success).availabilities
                            .filter { selectedSlotIds.contains(it.id) }
                            .map { it.availableDate }
                            .toSet()
                    } else emptySet()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(280.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(calendarArrangement) { date ->
                        if (date != null) {
                            val todayCal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                            }
                            val isPast = date.before(todayCal.time) && !isSameDay(date, today.time)
                            val isSelected = isSameDay(date, selectedDate)
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                            val hasAvailable = datesWithSlots.contains(dateStr)
                            val hasSelected = datesWithSelectedSlots.contains(dateStr)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        when {
                                            isSelected && hasSelected -> Orange
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable(enabled = !isPast) { viewModel.selectDate(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        SimpleDateFormat("d", Locale.getDefault()).format(date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.onBackground
                                        },
                                        fontSize = 13.sp
                                    )
                                    // Dot indicator cho ngày có slot
                                    if (!isPast && (hasAvailable || hasSelected)) {
                                        Spacer(Modifier.height(1.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(
                                                    if (isSelected) Color.White
                                                    else if (hasSelected) Orange
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Slot Section ──────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                val displayDate = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(selectedDate)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Slot ngày $displayDate",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedSlotIds.isNotEmpty()) {
                        TextButton(
                            onClick = { viewModel.clearSelectedSlots() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Xóa tất cả", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Hint text
                Text(
                    "💡 Chọn nhiều slot từ nhiều ngày để thanh toán 1 lần",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (val state = uiState) {
                    is PtBookingUiState.Loading -> {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is PtBookingUiState.Success -> {
                        val slotsForDay = state.availabilities.filter {
                            it.availableDate == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                        }
                        if (slotsForDay.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    Text("Không có lịch trống ngày này", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Thử chọn ngày khác có dấu chấm", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f))
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.heightIn(max = 220.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                userScrollEnabled = false
                            ) {
                                items(slotsForDay) { slot ->
                                    MultiSlotItem(
                                        timeDisplay = "${formatTime(slot.startTime)} - ${formatTime(slot.endTime)}",
                                        isBooked = slot.isBooked,
                                        isSelected = selectedSlotIds.contains(slot.id),
                                        onClick = { if (!slot.isBooked) viewModel.toggleSlot(slot.id) }
                                    )
                                }
                            }
                        }
                    }
                    is PtBookingUiState.Error -> {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> {}
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Provider Section ──────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    "Thanh toán qua",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ProviderItem(
                    name = "VNPay", isSelected = selectedProvider == "VNPAY",
                    onClick = { viewModel.selectProvider("VNPAY") }
                )
                Spacer(Modifier.height(8.dp))
                ProviderItem(
                    name = "Ví MoMo", isSelected = selectedProvider == "MOMO",
                    onClick = { viewModel.selectProvider("MOMO") }
                )
            }

            // ── Reviews Section ───────────────────────────────────────────
            val reviews = ptDetail?.reviews ?: emptyList()
            if (reviews.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Đánh giá từ học viên (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    reviews.forEach { review ->
                        ReviewItem(review = review)
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Multi-select Slot Item ─────────────────────────────────────────────────────
@Composable
private fun MultiSlotItem(
    timeDisplay: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> Orange.copy(alpha = 0.12f)
            isBooked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200), label = "slotBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Orange
            isBooked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(200), label = "slotBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .border(
                if (isSelected) 2.dp else 1.dp,
                borderColor,
                MaterialTheme.shapes.extraSmall
            )
            .clickable(enabled = !isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Orange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.height(1.dp))
            }
            Text(
                timeDisplay,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isSelected -> Orange
                    isBooked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.onBackground
                },
                textDecoration = if (isBooked) TextDecoration.LineThrough else null,
                fontSize = if (isSelected) 11.sp else 12.sp
            )
        }
    }
}

@Composable
private fun ProviderItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected, onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

private fun isSameDay(d1: Date, d2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = d1 }
    val cal2 = Calendar.getInstance().apply { time = d2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun formatVnd(amount: Long): String {
    return try {
        val fmt = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        "${fmt.format(amount)}đ"
    } catch (e: Exception) { "${amount}đ" }
}

/**
 * Chuẩn hóa chuỗi thời gian từ backend ("09:00:00", "9:00:00", "09:00") → "HH:mm"
 */
private fun formatTime(timeStr: String): String {
    return try {
        val parts = timeStr.trim().split(":")
        val h = parts.getOrElse(0) { "0" }.toInt()
        val m = parts.getOrElse(1) { "0" }.toInt()
        String.format("%02d:%02d", h, m)
    } catch (e: Exception) {
        if (timeStr.length >= 5) timeStr.substring(0, 5) else timeStr
    }
}

@Composable
private fun ReviewItem(review: PtReviewDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Avatar
        if (review.avatarUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    review.userName?.take(1)?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            AsyncImage(
                model = review.avatarUrl,
                contentDescription = review.userName,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    review.userName ?: "Ẩn danh",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E)
                )
                Row {
                    repeat(review.rating) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    repeat(5 - review.rating) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFE0E0E0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    review.comment,
                    fontSize = 13.sp,
                    color = Color(0xFF555555),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

