package com.gymapp.android.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel

// ── Design tokens ─────────────────────────────────────────────────────────────
private val BgColor       = Color(0xFFF8F9FA)
private val CardBg        = Color.White
private val OrangePrimary = Color(0xFFFF5722)
private val OrangeDark    = Color(0xFFBF360C)
private val TextDark      = Color(0xFF111827)
private val TextGray      = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val GreenActive   = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScheduleSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf<String?>(null) }   // day code đang chọn giờ

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lịch tập hàng tuần", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(OrangePrimary, OrangeDark))
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Lịch nhắc tập",
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Chọn ngày & giờ nhận thông báo nhắc tập",
                                color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Summary badges — bao nhiêu ngày đang bật
            val enabledCount = uiState.schedules.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null,
                    tint = if (enabledCount > 0) GreenActive else TextGray,
                    modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    if (enabledCount == 0) "Chưa đặt lịch tập nào"
                    else "Đang tập: $enabledCount ngày/tuần",
                    fontWeight = FontWeight.Medium,
                    color = if (enabledCount > 0) GreenActive else TextGray,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Danh sách ngày
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            } else {
                Text("NGÀY TRONG TUẦN", color = TextGray, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column {
                        ALL_DAYS.forEachIndexed { index, day ->
                            DayScheduleRow(
                                day = day,
                                label = DAY_LABELS[day] ?: day,
                                isEnabled = uiState.schedules.containsKey(day),
                                remindTime = uiState.schedules[day] ?: "06:00",
                                onToggle = { viewModel.toggleDay(day) },
                                onSelectTime = { showTimePicker = day }
                            )
                            if (index < ALL_DAYS.lastIndex)
                                HorizontalDivider(color = DividerColor, thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Text("Lưu lịch tập", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Hệ thống sẽ gửi thông báo nhắc tập vào 7:00 sáng mỗi ngày bạn chọn.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
    }

    // Time Picker Dialog
    showTimePicker?.let { day ->
        TimePickerDialog(
            initialTime = uiState.schedules[day] ?: "06:00",
            dayLabel = DAY_LABELS[day] ?: day,
            onConfirm = { time ->
                viewModel.setRemindTime(day, time)
                showTimePicker = null
            },
            onDismiss = { showTimePicker = null }
        )
    }
}

// ── Day row ───────────────────────────────────────────────────────────────────

@Composable
private fun DayScheduleRow(
    day: String,
    label: String,
    isEnabled: Boolean,
    remindTime: String,
    onToggle: () -> Unit,
    onSelectTime: () -> Unit
) {
    // Ngày viết tắt Tiếng Anh 2 ký tự
    val shortDay = when (day) {
        "MON" -> "T2"; "TUE" -> "T3"; "WED" -> "T4"
        "THU" -> "T5"; "FRI" -> "T6"; "SAT" -> "T7"; "SUN" -> "CN"
        else  -> day.take(2)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isEnabled) OrangePrimary else Color(0xFFF3F4F6),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = shortDay,
                color = if (isEnabled) Color.White else TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextDark, fontSize = 15.sp,
                fontWeight = if (isEnabled) FontWeight.SemiBold else FontWeight.Normal)
            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text("Nhắc lúc $remindTime",
                    color = OrangePrimary, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp))
            }
        }

        // Time picker trigger (chỉ hiện khi bật)
        AnimatedVisibility(visible = isEnabled) {
            Row(
                modifier = Modifier
                    .border(1.dp, OrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onSelectTime)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null,
                    tint = OrangePrimary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(remindTime, color = OrangePrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(8.dp))

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = OrangePrimary,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = Color(0xFFF3F4F6),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// ── Time Picker Dialog ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    dayLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = initialTime.split(":").map { it.toIntOrNull() ?: 0 }
    val timePickerState = rememberTimePickerState(
        initialHour = parts.getOrElse(0) { 6 },
        initialMinute = parts.getOrElse(1) { 0 },
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Giờ nhắc — $dayLabel",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Spacer(Modifier.height(16.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFFFF3F0),
                        selectorColor = OrangePrimary,
                        containerColor = CardBg,
                        timeSelectorSelectedContainerColor = OrangePrimary,
                        timeSelectorSelectedContentColor = Color.White
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = TextGray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val h = timePickerState.hour.toString().padStart(2, '0')
                            val m = timePickerState.minute.toString().padStart(2, '0')
                            onConfirm("$h:$m")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
