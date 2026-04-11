package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val Ttertiary      = Color(0xFFAAAAAA)
private val GreenPrimary   = Color(0xFF1D9E75)
private val GreenDark      = Color(0xFF085041)
private val GreenLight     = Color(0xFFE1F5EE)
private val GreenBorder    = Color(0xFF9FE1CB)
private val BlueActive     = Color(0xFF185FA5)

// ── Screen 7: PT Client Progress ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClientProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                            Text("Tổng: ${viewModel.totalSessions} buổi · Buổi cuối: $dateInfo", fontSize = 13.sp, color = Tsecondary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgSecondary
                ),
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
                            Button(onClick = { viewModel.loadProgress() }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                is ClientProgressUiState.Success -> {
                    val progress = state.progress
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {

                        // Workout Logs
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("Nhật ký buổi tập", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tsecondary)
                            Spacer(Modifier.height(8.dp))
                        }

                        items(progress.sessions) { session ->
                            WorkoutLogItem(
                                title = "Buổi tập: ${session.status}",
                                date = formatDatetime(session.date),
                                notes = session.workoutLogs?.firstOrNull()?.notes ?: "Chưa có ghi chú",
                                isDone = session.status == "COMPLETED"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutLogItem(title: String, date: String, notes: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .background(if (isDone) GreenPrimary else BlueActive, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDone) Tprimary else Tsecondary)
            Text(notes, fontSize = 14.sp, color = if (isDone) Tsecondary else Ttertiary)
        }
        Text(date, fontSize = 12.sp, color = Ttertiary)
    }
}
