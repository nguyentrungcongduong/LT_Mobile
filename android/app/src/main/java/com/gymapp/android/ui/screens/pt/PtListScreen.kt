package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gymapp.android.data.remote.api.PtPublicDto

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val PinkPrimary    = Color(0xFFFF5722) // Adjusted from standard red to a vibrant orange-red
private val BlueActive     = Color(0xFF185FA5)
private val AmberStar      = Color(0xFFFFB300)

@Composable
fun PtListScreen(
    onNavigateToBooking: (String) -> Unit,
    viewModel: PtListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .border(0.5.dp, BorderTertiary)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Đội ngũ PT chuyên nghiệp", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tprimary)
        }

        when (val state = uiState) {
            is PtListUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueActive)
                }
            }
            is PtListUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Tsecondary)
                        TextButton(onClick = { viewModel.loadPts() }) {
                            Text("Thử lại", color = BlueActive)
                        }
                    }
                }
            }
            is PtListUiState.Success -> {
                if (state.pts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ghi nhận 0 PT, có thể Backend chưa đổ Data cho bảng Trainers", 
                            color = Tsecondary, 
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.pts) { pt ->
                            PtCard(pt = pt, onClick = { onNavigateToBooking(pt.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PtCard(pt: PtPublicDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BgPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderTertiary)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // PT Avatar
            if (pt.avatarUrl.isNullOrBlank()) {
                AvatarBubble(name = pt.fullName, modifier = Modifier.size(70.dp))
            } else {
                AsyncImage(
                    model = pt.avatarUrl,
                    contentDescription = pt.fullName,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderTertiary, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(pt.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                Text(pt.specializations?.firstOrNull() ?: "Personal Trainer", fontSize = 14.sp, color = Tsecondary)
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberStar, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${pt.rating}", fontSize = 14.sp, fontWeight = FontWeight.W500, color = Tprimary)
                    Spacer(Modifier.width(4.dp))
                    Text("(${pt.reviewCount})", fontSize = 12.sp, color = Tsecondary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%,.0fđ".format(pt.price),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueActive
                )
                Text("/buổi", fontSize = 12.sp, color = Tsecondary)
                
                Spacer(Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .background(PinkPrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Đặt lịch", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
