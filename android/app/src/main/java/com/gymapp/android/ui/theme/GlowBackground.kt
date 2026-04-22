package com.gymapp.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Full-screen dark background (#121212) with ambient orange glow blobs.
 * Uses radialGradient Brush (GPU-accelerated, zero CPU overhead).
 */
@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {

        // ── Base black background ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
        )

        // ── Blob 1: top-right, large warm orange ───────────────────────────
        Box(
            modifier = Modifier
                .size(380.dp)
                .offset(x = 120.dp, y = (-60).dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x55FF6B2B),  // α=33%
                            Color(0x22FF5722),  // α=13%
                            Color(0x00FF5722)   // transparent
                        )
                    )
                )
        )

        // ── Blob 2: bottom-left, medium deep orange ────────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = 560.dp)
                .blur(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44FF5722),  // α=27%
                            Color(0x18FF4500),  // α=9%
                            Color(0x00FF4500)
                        )
                    )
                )
        )

        // ── Blob 3: center subtle accent ───────────────────────────────────
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 80.dp, y = 300.dp)
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x22FF8C00),  // α=13%
                            Color(0x00FF8C00)
                        )
                    )
                )
        )

        // ── Content on top ─────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
