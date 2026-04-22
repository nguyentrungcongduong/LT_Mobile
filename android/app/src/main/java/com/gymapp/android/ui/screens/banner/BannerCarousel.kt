package com.gymapp.android.ui.screens.banner

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun BannerCarousel(
    banners: List<com.gymapp.android.data.remote.response.BannerResponse>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(banners.size) {
        while (true) {
            delay(3000)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(5.dp))

        // ── Banner image with title/description overlay ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Ảnh nền
            AsyncImage(
                model = banners[currentIndex].imageUrl,
                contentDescription = banners[currentIndex].title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay phía dưới để text dễ đọc
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC000000)),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    // Tiêu đề
                    if (!banners[currentIndex].title.isNullOrBlank()) {
                        Text(
                            text = banners[currentIndex].title!!,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Mô tả
                    if (!banners[currentIndex].description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banners[currentIndex].description!!,
                            color = Color(0xCCFFFFFF),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ── Dot indicators ───────────────────────────────────────────────
        if (banners.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                banners.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(if (index == currentIndex) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) Color(0xFFFF8C00)
                                else Color(0xFFBDBDBD)
                            )
                    )
                }
            }
        }
    }
}