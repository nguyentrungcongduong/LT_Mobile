package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val BlueActive     = Color(0xFF185FA5)
private val BlueLight      = Color(0xFFE6F1FB)
private val PinkPrimary    = Color(0xFFFF5722)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtBookingScreen(
    onNavigateBack: () -> Unit,
    onNext: (String) -> Unit, // Navigate to confirmation screen
    viewModel: PtBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSlotId by viewModel.selectedSlotId.collectAsState()
    val viewingMonth by viewModel.viewingMonth.collectAsState()

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val today = Calendar.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt lịch PT", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                modifier = Modifier.border(0.5.dp, BorderTertiary)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPrimary)
                    .border(0.5.dp, BorderTertiary)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onNext(viewModel.ptId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueActive),
                    enabled = selectedSlotId != null
                ) {
                    Text("Tiếp theo — Xác nhận đặt lịch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
        ) {
            // Calendar Section (unchanged from previous version)
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthFormat.format(viewingMonth.time),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tprimary
                    )
                    Row {
                        IconButton(onClick = { viewModel.prevMonth() }) {
                            Text("<", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueActive)
                        }
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlueActive)
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
                            fontSize = 12.sp,
                            color = Ttertiary_alt
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
                    modifier = Modifier.height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> BlueActive
                                            isPast -> Color(0xFFFFF9C4)
                                            else -> BgSecondary
                                        }
                                    )
                                    .clickable(enabled = !isPast) { viewModel.selectDate(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = SimpleDateFormat("d", Locale.getDefault()).format(date),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isPast -> Color.LightGray
                                        else -> Tprimary
                                    }
                                )
                            }
                        } else {
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = BorderTertiary)

            // Slot Section
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val displayDate = SimpleDateFormat("dd/MM", Locale.getDefault()).format(selectedDate)
                Text(
                    text = "Slot ngày $displayDate — chọn 1 khung giờ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tprimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (val state = uiState) {
                    is PtBookingUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BlueActive)
                        }
                    }
                    is PtBookingUiState.Success -> {
                        if (state.availabilities.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Không có lịch trống ngày này", color = Tsecondary, fontSize = 14.sp)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.availabilities) { slot ->
                                    SlotItem(
                                        startTime = slot.startTime,
                                        endTime = slot.endTime,
                                        isBooked = slot.isBooked,
                                        isSelected = selectedSlotId == slot.id,
                                        onClick = { if (!slot.isBooked) viewModel.selectSlot(slot.id) }
                                    )
                                }
                            }
                        }
                    }
                    is PtBookingUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SlotItem(
    startTime: String,
    endTime: String,
    isBooked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected -> BlueActive
        isBooked -> Color.Transparent
        else -> BlueLight
    }
    val textColor = when {
        isSelected -> Color.White
        isBooked -> Tsecondary
        else -> BlueActive
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(
                width = if (isSelected || isBooked) 0.dp else 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(enabled = !isBooked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$startTime - $endTime",
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            textDecoration = if (isBooked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
        )
    }
}

private val Ttertiary_alt = Color(0xFFAAAAAA)

private fun isSameDay(d1: Date, d2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = d1 }
    val cal2 = Calendar.getInstance().apply { time = d2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
