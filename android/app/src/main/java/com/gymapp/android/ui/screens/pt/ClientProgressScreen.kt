package com.gymapp.android.ui.screens.pt

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.ClientProgressDto
import com.gymapp.android.data.remote.api.ClientSessionDto
import com.gymapp.android.data.remote.api.WorkoutLogDto
import java.text.SimpleDateFormat
import java.util.*

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val Ttertiary      = Color(0xFFAAAAAA)
private val GreenPrimary   = Color(0xFF1D9E75)
private val GreenLight     = Color(0xFFE1F5EE)
private val GreenBorder    = Color(0xFF9FE1CB)
private val BlueActive     = Color(0xFF185FA5)
private val BlueBg         = Color(0xFFE8F1FB)
private val BlueBorder     = Color(0xFFB0CCF0)
private val OrangePrimary  = Color(0xFFE87C2E)
private val OrangeBg       = Color(0xFFFFF0E6)
private val OrangeBorder   = Color(0xFFF5C89A)
private val RedPrimary     = Color(0xFFD9534F)
private val RedBg          = Color(0xFFFDECEC)
private val RedBorder      = Color(0xFFF3A9A8)
private val PendingPrimary = Color(0xFF8B5CF6)
private val PendingBg      = Color(0xFFF3EFFE)
private val PendingBorder  = Color(0xFFC4B0F5)

private fun formatSessionDatetime(dateInput: Any?): String {
    return try {
        val date = when (dateInput) {
            is String -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(dateInput)
            is Date -> dateInput
            else -> null
        } ?: return "-"
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(date)
    } catch (e: Exception) { "-" }
}

private fun formatSessionDateOnly(dateInput: Any?): String {
    return try {
        val date = when (dateInput) {
            is String -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(dateInput)
            is Date -> dateInput
            else -> null
        } ?: return "-"
        SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(date)
    } catch (e: Exception) { "-" }
}

private fun formatSessionTimeOnly(dateInput: Any?): String {
    return try {
        val date = when (dateInput) {
            is String -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(dateInput)
            is Date -> dateInput
            else -> null
        } ?: return "-"
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (e: Exception) { "-" }
}

data class StatusStyle(
    val label: String,
    val icon: ImageVector,
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color
)

private fun getStatusStyle(status: String): StatusStyle = when (status.uppercase()) {
    "COMPLETED" -> StatusStyle("Hoàn thành", Icons.Default.CheckCircle, GreenPrimary, GreenLight, GreenBorder)
    "CONFIRMED" -> StatusStyle("Xác nhận", Icons.Default.Event, BlueActive, BlueBg, BlueBorder)
    "PENDING"   -> StatusStyle("Chờ thanh toán", Icons.Default.HourglassEmpty, PendingPrimary, PendingBg, PendingBorder)
    "CANCELLED" -> StatusStyle("Đã hủy", Icons.Default.Cancel, RedPrimary, RedBg, RedBorder)
    else        -> StatusStyle(status, Icons.Default.Info, Tsecondary, BgSecondary, BorderTertiary)
}

// ── Screen: PT Client Progress ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClientProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Lịch đặt", "Nhật ký")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarBubble(name = viewModel.clientName, bgColor = AvatarBlueBg)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            val dateInfo = if (viewModel.lastSessionAt == "none") "-" else viewModel.lastSessionAt
                            Text(viewModel.clientName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Tổng: ${viewModel.totalSessions} buổi · Buổi cuối: $dateInfo",
                                fontSize = 13.sp, color = Tsecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                modifier = Modifier.border(0.5.dp, BorderTertiary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
        ) {
            // ── Tab Row ───────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BgSecondary,
                contentColor = GreenPrimary,
                divider = { HorizontalDivider(thickness = 0.5.dp, color = BorderTertiary) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────
            when (val state = uiState) {
                is ClientProgressUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is ClientProgressUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Tsecondary)
                            Button(
                                onClick = { viewModel.loadProgress() },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) { Text("Thử lại") }
                        }
                    }
                }
                is ClientProgressUiState.Success -> {
                    when (selectedTab) {
                        0 -> SessionsTab(sessions = state.progress.sessions)
                        1 -> WorkoutLogsTab(sessions = state.progress.sessions)
                    }
                }
            }
        }
    }
}

// ── Tab 0: Lịch đặt ────────────────────────────────────────────────────────────
@Composable
private fun SessionsTab(sessions: List<ClientSessionDto>?) {
    if (sessions.isNullOrEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(48.dp), tint = Ttertiary)
                Text("Chưa có lịch đặt nào", color = Tsecondary, fontSize = 15.sp)
            }
        }
        return
    }

    // Stats row
    val completed = sessions.count { it.status == "COMPLETED" }
    val confirmed = sessions.count { it.status == "CONFIRMED" }
    val cancelled = sessions.count { it.status == "CANCELLED" }
    val pending   = sessions.count { it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SessionStatChip("✅ $completed", "Hoàn thành", GreenPrimary, GreenLight, Modifier.weight(1f))
                SessionStatChip("📅 $confirmed", "Xác nhận", BlueActive, BlueBg, Modifier.weight(1f))
                SessionStatChip("❌ $cancelled", "Đã hủy", RedPrimary, RedBg, Modifier.weight(1f))
            }
        }

        // Session cards
        item {
            Text(
                "Danh sách ${sessions.size} lịch đặt",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = Tsecondary, modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        items(sessions) { session ->
            SessionCard(session = session)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SessionStatChip(
    value: String,
    label: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = textColor.copy(alpha = 0.75f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun SessionCard(session: ClientSessionDto) {
    val style = getStatusStyle(session.status ?: "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date box
        Column(
            modifier = Modifier
                .width(52.dp)
                .background(style.bgColor, RoundedCornerShape(8.dp))
                .border(0.5.dp, style.borderColor, RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dateParts = formatSessionDateOnly(session.date).split("/")
            Text(
                dateParts.getOrElse(0) { "--" },
                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = style.textColor
            )
            Text(
                "th${dateParts.getOrElse(1) { "--" }}",
                fontSize = 11.sp, color = style.textColor.copy(alpha = 0.75f)
            )
            Text(
                dateParts.getOrElse(2) { "----" },
                fontSize = 10.sp, color = style.textColor.copy(alpha = 0.55f)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Tsecondary)
                Text(formatSessionTimeOnly(session.date), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Tprimary)
            }
            Row(
                modifier = Modifier
                    .background(style.bgColor, RoundedCornerShape(6.dp))
                    .border(0.5.dp, style.borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(style.icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = style.textColor)
                Text(style.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = style.textColor)
            }
        }
    }
}

// ── Tab 1: Nhật ký ─────────────────────────────────────────────────────────────
@Composable
private fun WorkoutLogsTab(sessions: List<ClientSessionDto>?) {
    val completedSessions: List<ClientSessionDto> = sessions?.filter { it.status == "COMPLETED" } ?: emptyList()

    if (completedSessions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(48.dp), tint = Ttertiary)
                Text("Chưa có buổi tập hoàn thành", color = Tsecondary, fontSize = 15.sp)
                Text("Nhật ký sẽ hiện sau khi buổi tập kết thúc", color = Ttertiary, fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Nhật ký buổi tập", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Tsecondary)
            Spacer(Modifier.height(6.dp))
        }
        items(completedSessions) { session ->
            WorkoutLogItem(
                title = "Buổi tập hoàn thành",
                date = formatSessionDatetime(session.date),
                notes = session.workoutLogs?.firstOrNull()?.notes ?: "Chưa có ghi chú",
                isDone = true
            )
        }
    }
}

@Composable
private fun WorkoutLogItem(title: String, date: String, notes: String, isDone: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgSecondary)
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(if (isDone) GreenPrimary else BlueActive, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isDone) Tprimary else Tsecondary)
            Spacer(Modifier.height(4.dp))
            Text(notes, fontSize = 13.sp, color = Tsecondary, lineHeight = 18.sp)
        }
        Text(date, fontSize = 12.sp, color = Ttertiary)
    }
}


