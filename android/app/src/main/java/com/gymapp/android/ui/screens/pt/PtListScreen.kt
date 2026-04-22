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
import androidx.compose.ui.graphics.Brush
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

// ── Design Tokens (Dark Theme) ─────────────────────────────────────────────────
private val BgPrimary      = Color(0xFF121212)
private val BgCard         = Color(0xFF1C1C1E)
private val BgCardFooter   = Color(0xFF252528)
private val BorderColor    = Color(0xFF2A2A2E)
private val Tprimary       = Color(0xFFF2F2F2)
private val Tsecondary     = Color(0xFF9A9A9E)
private val OrangePrimary  = Color(0xFFFF6B2B)
private val AmberStar      = Color(0xFFFFB300)
private val BlueChipBg     = Color(0xFF1A2A3A)
private val BlueChipText   = Color(0xFF64B5F6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtListScreen(
    onNavigateToBooking: (String) -> Unit,
    viewModel: PtListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Track which PT to rate for the bottom sheet
    var ratingTarget by remember { mutableStateOf<PtPublicDto?>(null) }

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
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1C1C1E), Color(0xFF252528))
                    )
                )
                .border(
                    width = 0.5.dp,
                    color = BorderColor,
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "Đội ngũ PT chuyên nghiệp",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Tprimary
            )
        }

        when (val state = uiState) {
            is PtListUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
            is PtListUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = Tsecondary)
                        TextButton(onClick = { viewModel.loadPts() }) {
                            Text("Thử lại", color = OrangePrimary)
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.pts) { pt ->
                            PtCard(
                                pt = pt,
                                onClick = { onNavigateToBooking(pt.id) },
                                onRate = { ratingTarget = pt }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Rating BottomSheet ───────────────────────────────────────────
    ratingTarget?.let { pt ->
        RatePtBottomSheet(
            ptId = pt.id,
            ptName = pt.fullName,
            onDismiss = { ratingTarget = null }
        )
    }
}

@Composable
private fun PtCard(pt: PtPublicDto, onClick: () -> Unit, onRate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PT Avatar with Fallback
            if (pt.avatarUrl.isNullOrBlank()) {
                AvatarBubble(
                    name = pt.fullName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
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
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
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

                Spacer(Modifier.height(5.dp))

                // Chip Specialization
                val specText = pt.specializations?.firstOrNull() ?: "Personal Trainer"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BlueChipBg,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = specText,
                        color = BlueChipText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AmberStar,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${pt.rating}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tprimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "(${pt.reviewCount} đánh giá)",
                        fontSize = 12.sp,
                        color = Tsecondary
                    )
                }
            }
        }

        HorizontalDivider(color = BorderColor, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCardFooter)
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
                        color = OrangePrimary
                    )
                    Text(
                        " /buổi",
                        fontSize = 13.sp,
                        color = Tsecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Rate button
                OutlinedButton(
                    onClick = onRate,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberStar),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberStar)
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AmberStar
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Đánh giá", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberStar)
                }

                // Book button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Đặt lịch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
