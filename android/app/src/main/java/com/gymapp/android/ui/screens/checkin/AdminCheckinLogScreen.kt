package com.gymapp.android.ui.screens.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
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
import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Dark Design Tokens ──────────────────────────────────────────────────────
private val BgPrimary   = Color(0xFF121212)
private val BgSecondary = Color(0xFF1C1C1E)
private val BgCard      = Color(0xFF1E1E22)
private val BorderDark  = Color(0xFF2A2A2E)
private val Tprimary    = Color(0xFFF2F2F2)
private val Tsecondary  = Color(0xFF9A9A9E)
private val Orange      = Color(0xFFFF6B2B)
private val OrangeDim   = Color(0xFF2A1508)
private val GreenText   = Color(0xFF2ECC8E)
private val GreenBg     = Color(0xFF0D2B1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCheckinLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminCheckinLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(OrangeDim, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, null, tint = Orange, modifier = Modifier.size(16.dp))
                        }
                        Text("Lịch sử Check-in", fontWeight = FontWeight.Bold, color = Tprimary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Tprimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, null, tint = Orange)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary)
            )
        }
    ) { pad ->
        when (val state = uiState) {
            is CheckinLogUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(pad),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Orange) }
            }

            is CheckinLogUiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(pad),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.load() }) {
                            Text("Thử lại", color = Orange)
                        }
                    }
                }
            }

            is CheckinLogUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(pad)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp)
                ) {
                    // ── Summary stat ───────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GreenBg, RoundedCornerShape(14.dp))
                                .border(1.dp, GreenText.copy(0.25f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Tổng lượt check-in", fontSize = 12.sp, color = Tsecondary)
                                Text(
                                    "${state.total}",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GreenText
                                )
                            }
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = GreenText,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    // ── Section label ──────────────────────────────────────
                    item {
                        Text(
                            "Gần đây nhất",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Tsecondary
                        )
                    }

                    // ── Empty ────────────────────────────────────────────
                    if (state.logs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(top = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.History, null,
                                        tint = Tsecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text("Chưa có lượt check-in nào", color = Tsecondary)
                                }
                            }
                        }
                    }

                    // ── Log rows ──────────────────────────────────────────
                    itemsIndexed(state.logs) { index, log ->
                        CheckinLogRow(log = log, index = index + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckinLogRow(log: CheckinLogResponse, index: Int) {
    val timeFormatted = remember(log.checkinTime) {
        try {
            val odt = OffsetDateTime.parse(log.checkinTime)
            odt.format(DateTimeFormatter.ofPattern("HH:mm:ss · dd/MM/yyyy", Locale.getDefault()))
        } catch (e: Exception) {
            log.checkinTime
        }
    }

    // Avatar initials
    val initials = log.userFullName
        .split(" ")
        .filter { it.isNotBlank() }
        .takeLast(2)
        .joinToString("") { it.first().uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Index number
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(OrangeDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Orange)
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(log.userFullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Tprimary)
            Text(log.userEmail, fontSize = 11.sp, color = Tsecondary)
            Text(timeFormatted, fontSize = 12.sp, color = Tsecondary)
        }

        // Badge
        Box(
            modifier = Modifier
                .background(GreenBg, RoundedCornerShape(8.dp))
                .border(1.dp, GreenText.copy(0.3f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Vào", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GreenText)
        }
    }
}
