package com.gymapp.android.ui.screens.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val history by viewModel.checkinHistory.collectAsState()
    val isLoading by viewModel.isLoadingHistory.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCheckinHistory()
    }

    val checkinDates = viewModel.getCheckinDatesForMonth(selectedMonth)

    val (trainedDays, restDays) = viewModel.getMonthlyStats(selectedMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tập luyện", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ===== HEADER + STATS =====
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text("Tập luyện tháng này", fontWeight = FontWeight.Bold, color = Color.Black)

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Không tập luyện", fontSize = 12.sp)
                                        Text(restDays.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Đã tập luyện", color = Color.White, fontSize = 12.sp)
                                        Text(trainedDays.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // ===== MONTH NAV =====
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(onClick = {
                                    viewModel.changeMonth(selectedMonth.minusMonths(1))
                                }) {
                                    Icon(Icons.Default.ChevronLeft, null)
                                }

                                Text(
                                    selectedMonth.format(DateTimeFormatter.ofPattern("MM/yyyy")),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                IconButton(onClick = {
                                    viewModel.changeMonth(selectedMonth.plusMonths(1))
                                }) {
                                    Icon(Icons.Default.ChevronRight, null)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Row {
                                listOf("T2","T3","T4","T5","T6","T7","CN").forEach {
                                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Black)
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            CalendarGrid(
                                yearMonth = selectedMonth,
                                checkinDates = checkinDates
                            )
                        }
                    }
                }

                // ===== HISTORY =====
                item {
                    Text("Lịch sử tập luyện", fontWeight = FontWeight.Bold)
                }

                if (history.isEmpty()) {
                    item {
                        Text("Không có dữ liệu", color = Color.Gray)
                    }
                } else {
                    items(history) {
                        CheckinItem(it)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    checkinDates: Set<Int>
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value - 1

    val days = mutableListOf<LocalDate?>()

    repeat(startOffset) { days.add(null) }
    for (i in 1..daysInMonth) {
        days.add(LocalDate.of(yearMonth.year, yearMonth.month, i))
    }
    while (days.size % 7 != 0) {
        days.add(null)
    }
    Column {
        days.chunked(7).forEach { week ->
            Row {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val isChecked = date.dayOfMonth in checkinDates

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isChecked) Color(0xFF42A5F5)
                                    else Color(0xFFE0E0E0)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                date.dayOfMonth.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChecked) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckinItem(checkin: CheckinLogResponse) {

    val formattedDate = checkin.checkinDate?.let {
        LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } ?: ""

    val formattedTime = checkin.checkinTime?.let {
        it.substring(11, 16) // lấy HH:mm từ ISO string
    } ?: ""

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("📅 $formattedDate", fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text("🕒 $formattedTime", color = Color.Black)
            Text(
                "📍 ${checkin.branchName ?: "Chi nhánh"}",
                fontSize = 12.sp,
                color = Color.Blue
            )
        }
    }
}