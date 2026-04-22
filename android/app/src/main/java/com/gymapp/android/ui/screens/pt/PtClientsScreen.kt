package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.ClientDto
import java.text.SimpleDateFormat
import java.util.*

// ── Dark Design Tokens ─────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFF121212)
private val BgSecondary    = Color(0xFF1C1C1E)
private val BgCard         = Color(0xFF1E1E22)
private val BorderDark     = Color(0xFF2A2A2E)
private val Tprimary       = Color(0xFFF2F2F2)
private val Tsecondary     = Color(0xFF9A9A9E)
private val Orange         = Color(0xFFFF6B2B)
private val OrangeGlow     = Color(0xFFFF8C00)
private val OrangeDim      = Color(0xFF2A1508)
private val ActiveBg       = Color(0xFF0D2B1E)
private val ActiveText     = Color(0xFF2ECC8E)
private val InactiveBg     = Color(0xFF1E1E22)
private val InactiveText   = Color(0xFF9A9A9E)

private fun formatDate(date: Date?): String {
    if (date == null) return "-"
    return SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
}

// ── Screen: PT Clients List ───────────────────────────────────────────────────
@Composable
fun PtClientsScreen(
    onNavigateToClientProgress: (String, String, Long, String?) -> Unit = { _, _, _, _ -> },
    viewModel: PtClientsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadClients() }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(BgSecondary, BgPrimary))
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(OrangeDim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.People, null, tint = Orange, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        "Clients của tôi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Tprimary
                    )
                }
                if (uiState is PtClientsUiState.Success) {
                    val count = (uiState as PtClientsUiState.Success).clients.size
                    Box(
                        modifier = Modifier
                            .background(OrangeDim, RoundedCornerShape(20.dp))
                            .border(1.dp, Orange.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "$count clients",
                            fontSize = 13.sp,
                            color = Orange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // ── Content ────────────────────────────────────────────────────────
        when (val state = uiState) {
            is PtClientsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            }
            is PtClientsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadClients() }) {
                            Text("Thử lại", color = Orange)
                        }
                    }
                }
            }
            is PtClientsUiState.Success -> {
                val filtered = if (searchQuery.isBlank()) state.clients
                else state.clients.filter { it.fullName.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Search Bar ─────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSecondary, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = Tsecondary)
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(color = Tprimary, fontSize = 14.sp),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text("Tìm kiếm client...", fontSize = 14.sp, color = Tsecondary)
                                    }
                                    inner()
                                }
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    // ── Empty State ────────────────────────────────────────
                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(BgSecondary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.People, null, tint = Tsecondary, modifier = Modifier.size(36.dp))
                                    }
                                    Text(
                                        if (searchQuery.isEmpty()) "Chưa có client nào" else "Không tìm thấy \"$searchQuery\"",
                                        color = Tprimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Học viên đặt lịch với bạn sẽ xuất hiện ở đây",
                                        color = Tsecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // ── Client List ────────────────────────────────────────
                    items(filtered.withIndex().toList()) { (index, client) ->
                        ClientRow(
                            client = client,
                            colorIndex = index % AvatarColors.size,
                            onClick = {
                                val dateStr = client.lastSessionAt?.let {
                                    SimpleDateFormat("dd-MM-yyyy", Locale.US).format(it)
                                }
                                onNavigateToClientProgress(client.userId, client.fullName, client.totalSessions, dateStr)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientRow(client: ClientDto, colorIndex: Int, onClick: () -> Unit) {
    val (bgColor, textColor) = AvatarColors[colorIndex]
    val isActive = client.totalSessions > 0L

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        avatarInitials(client.fullName),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(client.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(
                        "Buổi cuối: ${formatDate(client.lastSessionAt)} · ${client.totalSessions} buổi",
                        fontSize = 13.sp,
                        color = Tsecondary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) ActiveBg else InactiveBg,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isActive) ActiveText.copy(0.3f) else BorderDark,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        if (isActive) "Đang tập" else "Không tập",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) ActiveText else InactiveText
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Tsecondary
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = BorderDark)
    }
}

@Composable
private fun ClientStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Tsecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Orange)
    }
}
