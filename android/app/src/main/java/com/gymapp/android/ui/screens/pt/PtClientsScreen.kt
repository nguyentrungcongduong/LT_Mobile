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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.ClientDto
import java.text.SimpleDateFormat
import java.util.*

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val GreenPrimary   = Color(0xFF1D9E75)
private val GreenLight     = Color(0xFFE1F5EE)
private val GreenBorder    = Color(0xFF9FE1CB)
private val ConfirmedBg    = Color(0xFFEAF3DE)
private val ConfirmedBorder= Color(0xFFC0DD97)
private val ConfirmedText  = Color(0xFF3B6D11)
private val CompletedBg    = Color(0xFFF1EFE8)
private val CompletedBorder= Color(0xFFD3D1C7)
private val CompletedText  = Color(0xFF5F5E5A)


private fun formatDate(date: Date?): String {
    if (date == null) return "-"
    return SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
}

// ── Screen 6: PT Clients List ─────────────────────────────────────────────────
@Composable
fun PtClientsScreen(
    onNavigateToClientProgress: (String, String, Long, String?) -> Unit = { _, _, _, _ -> },
    viewModel: PtClientsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .border(0.5.dp, BorderTertiary)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Clients của tôi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tprimary)
            if (uiState is PtClientsUiState.Success) {
                Text(
                    text = "${(uiState as PtClientsUiState.Success).clients.size} clients",
                    fontSize = 14.sp,
                    color = Tsecondary
                )
            }
        }

        when (val state = uiState) {
            is PtClientsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            }
            is PtClientsUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.message, color = Tsecondary, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadClients() }) {
                            Text("Thử lại", color = GreenPrimary)
                        }
                    }
                }
            }
            is PtClientsUiState.Success -> {
                val filtered = if (searchQuery.isBlank()) state.clients
                else state.clients.filter { it.fullName.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Search Bar
                    item {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BgSecondary, RoundedCornerShape(7.dp))
                                .border(0.5.dp, BorderTertiary, RoundedCornerShape(7.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                modifier = Modifier.size(14.dp),
                                tint = Tsecondary
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) {
                                        Text("Tìm kiếm client...", fontSize = 14.sp, color = Tsecondary)
                                    }
                                    inner()
                                }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    // Client List
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(0.dp, Color.Transparent)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(avatarInitials(client.fullName), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
                Column {
                    Text(client.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Text(
                        "Buổi cuối: ${formatDate(client.lastSessionAt)} · ${client.totalSessions} buổi tổng",
                        fontSize = 14.sp,
                        color = Tsecondary
                    )
                }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .border(0.5.dp, ConfirmedBorder, RoundedCornerShape(8.dp))
                        .background(ConfirmedBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Đang tập", fontSize = 12.sp, fontWeight = FontWeight.W500, color = ConfirmedText)
                }
            } else {
                Box(
                    modifier = Modifier
                        .border(0.5.dp, CompletedBorder, RoundedCornerShape(8.dp))
                        .background(CompletedBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Không tập", fontSize = 12.sp, fontWeight = FontWeight.W500, color = CompletedText)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Tsecondary
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = BorderTertiary, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun ClientStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BgSecondary, RoundedCornerShape(10.dp))
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Tsecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
    }
}
