package com.gymapp.android.ui.screens.pt

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.PtAvailabilityDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Colors ──────────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFFF5722)
private val OrangeLight = Color(0xFFFFF3E0)
private val BgGray = Color(0xFFF8F9FA)
private val CardWhite = Color.White
private val TextMain = Color(0xFF111827)
private val TextSub = Color(0xFF6B7280)
private val Divider = Color(0xFFE5E7EB)

// ─── Preset time slots ────────────────────────────────────────────────────────
private val PRESET_SLOTS = listOf(
    Pair("06:00", "07:00"),
    Pair("07:00", "08:00"),
    Pair("08:00", "09:00"),
    Pair("09:00", "10:00"),
    Pair("10:00", "11:00"),
    Pair("14:00", "15:00"),
    Pair("15:00", "16:00"),
    Pair("16:00", "17:00"),
    Pair("17:00", "18:00"),
    Pair("18:00", "19:00"),
    Pair("19:00", "20:00"),
    Pair("20:00", "21:00")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtScheduleManagementScreen(
    currentPtId: String,
    viewModel: PtScheduleManagementViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val mySlots by viewModel.mySlots.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var viewingDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(currentPtId) {
        if (currentPtId.isNotBlank()) {
            viewModel.setCurrentPtId(currentPtId)
        }
    }

    // Re-load when selected date changes
    LaunchedEffect(viewingDate) {
        viewModel.selectDate(viewingDate)
        if (currentPtId.isNotBlank()) {
            viewModel.loadSlots()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Quản lý Lịch dạy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextMain
                        )
                        Text(
                            "Thêm slot để học viên đặt lịch",
                            fontSize = 13.sp,
                            color = TextSub
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(OrangePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Thêm slot",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgGray)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = OrangePrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm lịch dạy", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = BgGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Mini Calendar strip ──────────────────────────────────────────
            DateStripSelector(
                selectedDate = viewingDate,
                onDateSelected = { viewingDate = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Summary row ─────────────────────────────────────────────────
            val slotsForDay = mySlots.filter { slot ->
                slot.availableDate == viewingDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Slot ngày ${viewingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain,
                    fontSize = 16.sp
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangeLight
                ) {
                    Text(
                        "${mySlots.size} slot tổng",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = OrangePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Slot list ────────────────────────────────────────────────────
            when (val state = uiState) {
                is PtScheduleUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }
                is PtScheduleUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.message, color = Color(0xFFE53935))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSlots() },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                            ) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                else -> {
                    if (mySlots.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = TextSub,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Chưa có lịch dạy nào",
                                    color = TextMain,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Nhấn nút + để thêm slot dạy\nHọc viên sẽ thấy và đặt lịch với bạn",
                                    color = TextSub,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Thêm lịch dạy đầu tiên", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(slotsForDay) { slot ->
                                SlotCard(slot = slot)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Slot Dialog ───────────────────────────────────────────────────────
    if (showAddDialog) {
        AddSlotDialog(
            initialDate = viewingDate,
            isCreating = isCreating,
            onDismiss = { showAddDialog = false },
            onConfirm = { date, startH, startM, endH, endM ->
                viewModel.createSlot(
                    date = date,
                    startHour = startH,
                    startMinute = startM,
                    endHour = endH,
                    endMinute = endM,
                    onSuccess = {
                        showAddDialog = false
                        Toast.makeText(context, "✅ Đã thêm slot thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "❌ $err", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

// ─── Date Strip ───────────────────────────────────────────────────────────────
@Composable
private fun DateStripSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val dates = (0..13).map { today.plusDays(it.toLong()) }
    val dayFormat = DateTimeFormatter.ofPattern("EEE", Locale("vi", "VN"))
    val dateFormat = DateTimeFormatter.ofPattern("dd")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dates.forEach { date ->
            val isSelected = date == selectedDate
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) OrangePrimary else Color.Transparent)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(dayFormat).uppercase(),
                    fontSize = 9.sp,
                    color = if (isSelected) Color.White else TextSub,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = date.format(dateFormat),
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else TextMain,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (date == today && !isSelected) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(OrangePrimary, CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Slot Card ────────────────────────────────────────────────────────────────
@Composable
private fun SlotCard(slot: PtAvailabilityDto) {
    val isBooked = slot.isBooked
    val bgColor = if (isBooked) Color(0xFFFFF8F5) else CardWhite
    val statusColor = if (isBooked) OrangePrimary else Color(0xFF16A34A)
    val statusText = if (isBooked) "Đã đặt" else "Còn trống"
    val statusBg = if (isBooked) OrangeLight else Color(0xFFDCFCE7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isBooked) OrangeLight else Color(0xFFF0FDF4),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isBooked) OrangePrimary else Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.startTime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextMain
                    )
                    Text(
                        text = "→  ${slot.endTime}",
                        fontSize = 13.sp,
                        color = TextSub
                    )
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Hiện tên học viên nếu slot đã được đặt
            if (isBooked && slot.bookedByName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFFFE0CC), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avatar initials
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(OrangePrimary, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = slot.bookedByName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = slot.bookedByName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextMain
                        )
                        Text(
                            text = "Học viên đã đặt buổi này",
                            fontSize = 12.sp,
                            color = TextSub
                        )
                    }
                }
            }
        }
    }
}


// ─── Add Slot Dialog ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSlotDialog(
    initialDate: LocalDate,
    isCreating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Int, Int, Int, Int) -> Unit
) {
    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val today = LocalDate.now()
    val next7Days = (0..6).map { today.plusDays(it.toLong()) }
    val dateFmt = DateTimeFormatter.ofPattern("dd/MM (EEE)", Locale("vi", "VN"))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm slot dạy", fontWeight = FontWeight.Bold, color = TextMain)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Chọn ngày ──────────────────────────────────────────────
                Text(
                    "Chọn ngày",
                    fontWeight = FontWeight.SemiBold,
                    color = TextSub,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    next7Days.forEach { date ->
                        val isSelected = date == selectedDate
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OrangePrimary else BgGray)
                                .border(
                                    1.dp,
                                    if (isSelected) OrangePrimary else Divider,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDate = date }
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                DateTimeFormatter.ofPattern("EEE", Locale("vi", "VN")).format(date).take(2).uppercase(),
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White else TextSub
                            )
                            Text(
                                DateTimeFormatter.ofPattern("dd").format(date),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextMain
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Chọn giờ preset ────────────────────────────────────────
                Text(
                    "Chọn khung giờ",
                    fontWeight = FontWeight.SemiBold,
                    color = TextSub,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Morning
                Text("Buổi sáng", fontSize = 12.sp, color = TextSub)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_SLOTS.take(5).forEachIndexed { idx, slot ->
                        val isSelected = selectedPresetIndex == idx
                        TimeChip(
                            label = slot.first,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPresetIndex = if (isSelected) null else idx }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Afternoon / Evening
                Text("Buổi chiều / tối", fontSize = 12.sp, color = TextSub)
                Spacer(modifier = Modifier.height(6.dp))

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_SLOTS.drop(5).take(4).forEachIndexed { i, slot ->
                        val idx = i + 5
                        val isSelected = selectedPresetIndex == idx
                        TimeChip(
                            label = slot.first,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPresetIndex = if (isSelected) null else idx }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_SLOTS.drop(9).forEachIndexed { i, slot ->
                        val idx = i + 9
                        val isSelected = selectedPresetIndex == idx
                        TimeChip(
                            label = slot.first,
                            isSelected = isSelected,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPresetIndex = if (isSelected) null else idx }
                        )
                    }
                    // Padding boxes
                    repeat(4 - PRESET_SLOTS.drop(9).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Preview
                selectedPresetIndex?.let { idx ->
                    val preset = PRESET_SLOTS[idx]
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OrangeLight
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = OrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${selectedDate.format(dateFmt)}   ${preset.first} → ${preset.second}",
                                color = OrangePrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val idx = selectedPresetIndex ?: return@Button
                    val preset = PRESET_SLOTS[idx]
                    val (sh, sm) = preset.first.split(":").map { it.toInt() }
                    val (eh, em) = preset.second.split(":").map { it.toInt() }
                    onConfirm(selectedDate, sh, sm, eh, em)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                enabled = !isCreating && selectedPresetIndex != null
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Tạo slot", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextSub)
            }
        }
    )
}

@Composable
private fun TimeChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) OrangePrimary else BgGray)
            .border(1.dp, if (isSelected) OrangePrimary else Divider, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextMain
        )
    }
}
