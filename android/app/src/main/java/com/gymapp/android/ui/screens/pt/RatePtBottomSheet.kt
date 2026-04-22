package com.gymapp.android.ui.screens.pt

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.SubmitReviewRequest

// ── Colors ─────────────────────────────────────────────────────────────────
private val StarActive  = Color(0xFFFFB300)
private val StarInact   = Color(0xFFE0E0E0)
private val OrangePrim  = Color(0xFFFF5722)
private val NavyDark    = Color(0xFF1A1A2E)
private val TextSec     = Color(0xFF777777)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatePtBottomSheet(
    ptId: String,
    ptName: String,
    onDismiss: () -> Unit,
    viewModel: RatePtViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedRating by remember { mutableIntStateOf(0) }
    var commentText   by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Reset state every time the sheet opens fresh
    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    // Auto dismiss ONLY after user submits (success state reached while sheet is open)
    var submittedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState is RatePtUiState.Success && submittedOnce) {
            kotlinx.coroutines.delay(1800)
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                "Đánh giá huấn luyện viên",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                ptName,
                fontSize = 15.sp,
                color = TextSec,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Star Row ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    val isSelected = star <= selectedRating
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = 0.4f),
                        label = "star_scale"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (isSelected) StarActive else StarInact,
                        label = "star_color"
                    )
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "$star sao",
                        tint = tint,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(scale)
                            .clickable { selectedRating = star }
                    )
                }
            }

            // Rating label
            Spacer(Modifier.height(8.dp))
            val ratingLabel = when (selectedRating) {
                1 -> "😞 Rất tệ"
                2 -> "😐 Chưa hài lòng"
                3 -> "🙂 Bình thường"
                4 -> "😊 Hài lòng"
                5 -> "🤩 Tuyệt vời!"
                else -> "Chọn số sao để đánh giá"
            }
            Text(ratingLabel, fontSize = 15.sp, color = if (selectedRating > 0) OrangePrim else TextSec, fontWeight = FontWeight.Medium)

            Spacer(Modifier.height(20.dp))

            // ── Comment box ─────────────────────────────────────────────
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Chia sẻ trải nghiệm của bạn... (tùy chọn)", color = Color(0xFFBBBBBB)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrim,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedTextColor = NavyDark,
                    unfocusedTextColor = NavyDark
                ),
                maxLines = 4
            )

            Spacer(Modifier.height(24.dp))

            // ── Submit / success / error ─────────────────────────────────
            when (val state = uiState) {
                is RatePtUiState.Initial, is RatePtUiState.Error -> {
                    if (state is RatePtUiState.Error) {
                        Text(
                            state.message,
                            color = Color(0xFFE53935),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedRating > 0)
                                    Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFFCCCCCC), Color(0xFFCCCCCC)))
                            )
                            .clickable(enabled = selectedRating > 0) {
                                submittedOnce = true
                                viewModel.submitReview(
                                    ptId = ptId,
                                    request = SubmitReviewRequest(
                                        rating = selectedRating,
                                        comment = commentText.trim().ifBlank { null }
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Gửi đánh giá",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is RatePtUiState.Loading -> {
                    CircularProgressIndicator(color = OrangePrim, modifier = Modifier.size(40.dp))
                }

                is RatePtUiState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Cảm ơn bạn đã đánh giá!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyDark)
                        Text("Đánh giá của bạn giúp cộng đồng tìm được PT phù hợp.", fontSize = 13.sp, color = TextSec, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
