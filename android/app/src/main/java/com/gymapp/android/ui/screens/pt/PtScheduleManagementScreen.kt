package com.gymapp.android.ui.screens.pt

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
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

// Chuẩn hóa "9:00:00" / "09:00:00" / "09:00" → "HH:mm"
private fun fmtTime(t: String): String = try {
    val p = t.trim().split(":")
    String.format("%02d:%02d", p[0].toInt(), p[1].toInt())
} catch (e: Exception) { if (t.length >= 5) t.substring(0, 5) else t }

// ─── Dark Design Tokens ────────────────────────────────────────────────────────
private val BgPrimary    = Color(0xFF121212)
private val BgSecondary  = Color(0xFF1C1C1E)
private val BgCard       = Color(0xFF1E1E22)
private val BorderDark   = Color(0xFF2A2A2E)
private val Tprimary     = Color(0xFFF2F2F2)
private val Tsecondary   = Color(0xFF9A9A9E)
private val Orange       = Color(0xFFFF6B2B)
private val OrangeGlow   = Color(0xFFFF8C00)
private val OrangeDim    = Color(0xFF2A1508)
private val GreenText    = Color(0xFF2ECC8E)
private val GreenBg      = Color(0xFF0D2B1E)

// ─── Preset time slots ─────────────────────────────────────────────────────────
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

    // Compute here so both Scaffold body AND AddSlotDialog can access it
    val slotsForDay = remember(mySlots, viewingDate) {
        mySlots.filter { slot ->
            slot.availableDate == viewingDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    LaunchedEffect(currentPtId) {
        if (currentPtId.isNotBlank()) viewModel.setCurrentPtId(currentPtId)
    }
    LaunchedEffect(viewingDate) {
        viewModel.selectDate(viewingDate)
        if (currentPtId.isNotBlank()) viewModel.loadSlots()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Quản lý Lịch dạy",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Tprimary
                        )
                        Text(
                            "Thêm slot để học viên đặt lịch",
                            fontSize = 13.sp,
                            color = Tsecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.linearGradient(listOf(OrangeGlow, Orange)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, "Thêm slot", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Orange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Thêm lịch dạy", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = BgPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Date strip ──────────────────────────────────────────────────
            DateStripSelector(
                selectedDate = viewingDate,
                onDateSelected = { viewingDate = it }
            )

            // ── Summary row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Slot ngày ${viewingDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                    fontWeight = FontWeight.SemiBold,
                    color = Tprimary,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .background(OrangeDim, RoundedCornerShape(12.dp))
                        .border(1.dp, Orange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${mySlots.size} slot tổng",
                        color = Orange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = BorderDark, thickness = 1.dp)

            // ── Slot list ────────────────────────────────────────────────────
            when (val state = uiState) {
                is PtScheduleUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }
                is PtScheduleUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF5350), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color(0xFFEF5350))
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSlots() },
                                colors = ButtonDefaults.buttonColors(containerColor = Orange)
                            ) { Text("Thử lại", color = Color.White) }
                        }
                    }
                }
                else -> {
                    if (slotsForDay.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(BgSecondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = Tsecondary, modifier = Modifier.size(40.dp))
                                }
                                Text("Chưa có lịch dạy nào", color = Tprimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Nhấn nút + để thêm slot dạy\nHọc viên sẽ thấy và đặt lịch với bạn",
                                    color = Tsecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Thêm lịch dạy đầu tiên", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                        ) {
                            items(slotsForDay) { slot -> SlotCard(slot = slot) }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSlotDialog(
            initialDate = viewingDate,
            isCreating = isCreating,
            existingSlots = slotsForDay, // truyền slot hiện có để block preset trùng
            onDismiss = { showAddDialog = false },
            onConfirm = { date, sh, sm, eh, em ->
                viewModel.createSlot(
                    date = date, startHour = sh, startMinute = sm, endHour = eh, endMinute = em,
                    onSuccess = {
                        showAddDialog = false
                        Toast.makeText(context, "✅ Đã thêm slot thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err -> Toast.makeText(context, "❌ $err", Toast.LENGTH_LONG).show() }
                )
            }
        )
    }
}

// ─── Date Strip ────────────────────────────────────────────────────────────────
// Dùng LazyRow để tránh dính, mỗi ngày là card riêng có padding đầy đủ
@Composable
private fun DateStripSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val dates = (0..13).map { today.plusDays(it.toLong()) }
    val dayFormat  = DateTimeFormatter.ofPattern("EEE", Locale("vi", "VN"))
    val dateFormat = DateTimeFormatter.ofPattern("dd")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSecondary)
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dates) { date ->
                val isSelected = date == selectedDate
                val isToday    = date == today

                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected)
                                Brush.verticalGradient(listOf(OrangeGlow, Orange))
                            else
                                Brush.verticalGradient(listOf(BgCard, BgCard))
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) Orange else if (isToday) Orange.copy(alpha = 0.4f) else BorderDark,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Day abbreviation
                    Text(
                        text = date.format(dayFormat).take(2).uppercase(),
                        fontSize = 10.sp,
                        color = if (isSelected) Color.White else Tsecondary,
                        fontWeight = FontWeight.Medium
                    )
                    // Date number
                    Text(
                        text = date.format(dateFormat),
                        fontSize = 16.sp,
                        color = if (isSelected) Color.White
                               else if (isToday) Orange
                               else Tprimary,
                        fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Normal
                    )
                    // "Hôm nay" dot indicator
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                color = if (isSelected) Color.White.copy(alpha = 0.7f)
                                        else if (isToday) Orange
                                        else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

// ─── Slot Card ─────────────────────────────────────────────────────────────────
@Composable
private fun SlotCard(slot: PtAvailabilityDto) {
    val isBooked     = slot.isBooked
    val statusColor  = if (isBooked) Orange else GreenText
    val statusBg     = if (isBooked) OrangeDim else GreenBg
    val statusText   = if (isBooked) "Đã đặt" else "Còn trống"
    val iconBg       = if (isBooked) OrangeDim else GreenBg

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isBooked) Orange.copy(alpha = 0.4f) else BorderDark),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(iconBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, tint = statusColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(fmtTime(slot.startTime), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Tprimary)
                    Text("→  ${fmtTime(slot.endTime)}", fontSize = 13.sp, color = Tsecondary)
                }
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (isBooked && slot.bookedByName != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderDark, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Orange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            slot.bookedByName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(slot.bookedByName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Tprimary)
                        Text("Học viên đã đặt buổi này", fontSize = 12.sp, color = Tsecondary)
                    }
                }
            }
        }
    }
}

// ─── Add Slot Dialog ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSlotDialog(
    initialDate: LocalDate,
    isCreating: Boolean,
    existingSlots: List<PtAvailabilityDto> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, Int, Int, Int, Int) -> Unit
) {
    var selectedPresetIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    val today     = LocalDate.now()
    val next7Days = (0..6).map { today.plusDays(it.toLong()) }
    val dateFmt   = DateTimeFormatter.ofPattern("dd/MM (EEE)", Locale("vi", "VN"))

    val DialogBg  = Color(0xFF1C1C1E)
    val FieldBg   = Color(0xFF252528)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(OrangeDim, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddCircle, null, tint = Orange, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text("Thêm slot dạy", fontWeight = FontWeight.Bold, color = Tprimary)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Chọn ngày ─────────────────────────────────────────────
                Text("Chọn ngày", fontWeight = FontWeight.SemiBold, color = Tsecondary, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))

                // 7 ngày dùng LazyRow để không bị dính
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(next7Days) { date ->
                        val isSelected = date == selectedDate
                        val isToday    = date == today

                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Orange else FieldBg)
                                .border(
                                    1.dp,
                                    if (isSelected) Orange else if (isToday) Orange.copy(0.4f) else BorderDark,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDate = date }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                DateTimeFormatter.ofPattern("EEE", Locale("vi", "VN")).format(date).take(2).uppercase(),
                                fontSize = 9.sp,
                                color = if (isSelected) Color.White else Tsecondary
                            )
                            Text(
                                DateTimeFormatter.ofPattern("dd").format(date),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else if (isToday) Orange else Tprimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Chọn giờ ──────────────────────────────────────────────
                Text("Chọn khung giờ", fontWeight = FontWeight.SemiBold, color = Tsecondary, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))

                Text("🌅  Buổi sáng", fontSize = 11.sp, color = Tsecondary)
                Spacer(Modifier.height(6.dp))
                // Compute which presets are already taken for selectedDate
                val takenPresets: Set<Int> = remember(existingSlots, selectedDate) {
                    PRESET_SLOTS.mapIndexedNotNull { idx, (start, _) ->
                        val h = start.split(":")[0].toInt()
                        val m = start.split(":")[1].toInt()
                        val matchesTaken = existingSlots.any { slot ->
                            try {
                                val sp = slot.startTime.trim().split(":")
                                sp[0].toInt() == h && sp[1].toInt() == m
                            } catch (e: Exception) { false }
                        }
                        if (matchesTaken) idx else null
                    }.toSet()
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRESET_SLOTS.take(5).forEachIndexed { idx, slot ->
                        val isTaken = takenPresets.contains(idx)
                        TimeChip(
                            label = slot.first,
                            isSelected = selectedPresetIndex == idx,
                            isTaken = isTaken,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!isTaken) selectedPresetIndex = if (selectedPresetIndex == idx) null else idx }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("🌇  Buổi chiều / tối", fontSize = 11.sp, color = Tsecondary)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRESET_SLOTS.drop(5).take(4).forEachIndexed { i, slot ->
                        val idx = i + 5
                        val isTaken = takenPresets.contains(idx)
                        TimeChip(
                            label = slot.first,
                            isSelected = selectedPresetIndex == idx,
                            isTaken = isTaken,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!isTaken) selectedPresetIndex = if (selectedPresetIndex == idx) null else idx }
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRESET_SLOTS.drop(9).forEachIndexed { i, slot ->
                        val idx = i + 9
                        val isTaken = takenPresets.contains(idx)
                        TimeChip(
                            label = slot.first,
                            isSelected = selectedPresetIndex == idx,
                            isTaken = isTaken,
                            modifier = Modifier.weight(1f),
                            onClick = { if (!isTaken) selectedPresetIndex = if (selectedPresetIndex == idx) null else idx }
                        )
                    }
                    repeat(4 - PRESET_SLOTS.drop(9).size) { Spacer(Modifier.weight(1f)) }
                }

                // Preview
                selectedPresetIndex?.let { idx ->
                    val preset = PRESET_SLOTS[idx]
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OrangeDim, RoundedCornerShape(12.dp))
                            .border(1.dp, Orange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = Orange)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${selectedDate.format(dateFmt)}   ${preset.first} → ${preset.second}",
                            color = Orange,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
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
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                enabled = !isCreating && selectedPresetIndex != null
            ) {
                if (isCreating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Tạo slot", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = Tsecondary) }
        }
    )
}

// ─── Time Chip ─────────────────────────────────────────────────────────────────
@Composable
private fun TimeChip(
    label: String,
    isSelected: Boolean,
    isTaken: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isTaken    -> Color(0xFF2A2A2E)       // greyed out
        isSelected -> Orange
        else       -> Color(0xFF252528)
    }
    val borderColor = when {
        isTaken    -> Color(0xFF3A3A3E)
        isSelected -> Orange
        else       -> BorderDark
    }
    val textColor = when {
        isTaken    -> Color(0xFF555558)       // dimmed
        isSelected -> Color.White
        else       -> Tprimary
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = !isTaken) { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                color = textColor
            )
            if (isTaken) {
                Text(
                    text = "✓",
                    fontSize = 9.sp,
                    color = Color(0xFF666669)
                )
            }
        }
    }
}
