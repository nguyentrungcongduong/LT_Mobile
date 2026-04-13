package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gymapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
    val selectedSlotId by viewModel.selectedSlotId.collectAsState()
    val viewingMonth by viewModel.viewingMonth.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    val today = Calendar.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt lịch PT", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val isEnabled = selectedSlotId != null && selectedProvider != null
                    Button(
                        onClick = { onNext(viewModel.ptId) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        enabled = isEnabled
                    ) {
                        Text(
                            "Tiếp theo: Xác nhận",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            // PT Info Strip
            ptDetail?.let { pt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
                        Text(pt.fullName, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
                        Text(pt.specializations?.joinToString(" · ") ?: "Personal Trainer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${pt.rating} (${pt.reviewCount})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Calendar Section
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthFormat.format(viewingMonth.time).replaceFirstChar { it.uppercase() },
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

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

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

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(260.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(calendarArrangement) { date ->
                        if (date != null) {
                            val isPast = date.before(today.apply { 
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.time) && !isSameDay(date, today.time)
                            
                            val isSelected = isSameDay(date, selectedDate)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable(enabled = !isPast) { viewModel.selectDate(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = SimpleDateFormat("d", Locale.getDefault()).format(date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.onBackground
                                    }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Slot Section
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                val displayDate = SimpleDateFormat("dd/MM", Locale("vi", "VN")).format(selectedDate)
                Text(
                    text = "Slot ngày $displayDate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (val state = uiState) {
                    is PtBookingUiState.Loading -> {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is PtBookingUiState.Success -> {
                        if (state.availabilities.isEmpty()) {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Không có lịch trống", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.heightIn(max = 200.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                userScrollEnabled = false
                            ) {
                                items(state.availabilities) { slot ->
                                    SlotItem(
                                        startTime = slot.startTime,
                                        isBooked = slot.isBooked,
                                        isSelected = selectedSlotId == slot.id,
                                        onClick = { if (!slot.isBooked) viewModel.selectSlot(slot.id) }
                                    )
                                }
                            }
                        }
                    }
                    is PtBookingUiState.Error -> {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> {}
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Provider Selection Section
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Thanh toán qua",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ProviderItem(
                    name = "VNPay",
                    isSelected = selectedProvider == "VNPAY",
                    onClick = { viewModel.selectProvider("VNPAY") }
                )
                Spacer(Modifier.height(8.dp))
                ProviderItem(
                    name = "Ví MoMo",
                    isSelected = selectedProvider == "MOMO",
                    onClick = { viewModel.selectProvider("MOMO") }
                )
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SlotItem(
    startTime: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isBooked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isBooked -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isBooked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(containerColor)
            .border(1.dp, borderColor, MaterialTheme.shapes.extraSmall)
            .clickable(enabled = !isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = startTime,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                textDecoration = if (isBooked) TextDecoration.LineThrough else null
            )
        }
    }
}

@Composable
private fun ProviderItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary, unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant)
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
