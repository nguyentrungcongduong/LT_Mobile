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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.request.ImageRequest
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PT Avatar with Fallback and Smooth Request
            if (pt.avatarUrl.isNullOrBlank()) {
                AvatarBubble(name = pt.fullName, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)))
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pt.avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = pt.fullName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pt.fullName, 
                    fontSize = 17.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Tprimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Chip Specialization
                val specText = pt.specializations?.firstOrNull() ?: "Personal Trainer"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = specText,
                        color = Color(0xFF1565C0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberStar, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${pt.rating}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                    Spacer(Modifier.width(4.dp))
                    Text("(${pt.reviewCount} đánh giá)", fontSize = 12.sp, color = Tsecondary)
                }
            }
        }
        
        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Giá ưu đãi", fontSize = 12.sp, color = Tsecondary)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "%,.0fđ".format(pt.price),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PinkPrimary
                    )
                    Text(" /buổi", fontSize = 13.sp, color = Tsecondary, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Xem chi tiết", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
