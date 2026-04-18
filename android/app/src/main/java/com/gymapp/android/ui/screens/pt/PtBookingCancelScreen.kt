package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary       = Color(0xFFFFFFFF)
private val BgSecondary     = Color(0xFFF5F5F5)
private val BorderTertiary  = Color(0xFFEBEBEB)
private val Tprimary        = Color(0xFF1A1A1A)
private val Tsecondary      = Color(0xFF6B6B6B)
private val BlueActive      = Color(0xFF185FA5)
private val BlueDeep        = Color(0xFF042C53)
private val AmberBg         = Color(0xFFFAEEDA)
private val AmberBorder     = Color(0xFFFAC775)
private val AmberText       = Color(0xFF854F0B)
private val AmberValue      = Color(0xFF633806)
private val RejectBg        = Color(0xFFFCEBEB)
private val RejectText      = Color(0xFFA32D2D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtBookingCancelScreen(
    bookingId: String,
    ptName: String,
    scheduledAt: String,
    amount: String,
    onNavigateBack: () -> Unit,
    viewModel: PtBookingCancelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy")
    
    // Parse numeric props
    val amountVal = remember(amount) { try { BigDecimal(amount) } catch(e:Exception) { BigDecimal.ZERO } }
    val scheduledAtTime = remember(scheduledAt) { 
        val ts = scheduledAt.toLongOrNull() ?: 0L
        if (ts > 0) java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(ts), java.time.ZoneId.systemDefault()) else null 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết lịch hẹn", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                modifier = Modifier.border(0.5.dp, BorderTertiary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, BorderTertiary, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarBubble(name = ptName, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(ptName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                        Text("Đã xác nhận", fontSize = 14.sp, color = Color(0xFF3B6D11), fontWeight = FontWeight.Medium)
                    }
                }
                
                HorizontalDivider(thickness = 0.5.dp, color = BorderTertiary)
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Thời gian", color = Tsecondary, fontSize = 14.sp)
                    Text(scheduledAtTime?.format(timeFormatter) ?: "", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Tổng phí", color = Tsecondary, fontSize = 14.sp)
                    Text(currencyFormat.format(amountVal), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BlueDeep)
                }
            }

            // Refund Calculation Logic
            val now = OffsetDateTime.now()
            val (refundPct, timeDiffStr) = if (scheduledAtTime != null) {
                val duration = Duration.between(now, scheduledAtTime)
                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60
                val pct = when {
                    hours >= 24 -> 100
                    hours >= 2 -> 50
                    else -> 0
                }
                Pair(pct, "${hours}h ${minutes}m")
            } else {
                Pair(0, "--")
            }
            
            val refundAmount = amountVal.multiply(BigDecimal(refundPct)).divide(BigDecimal(100))

            // Warning Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmberBg)
                    .border(1.dp, AmberBorder, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Thông tin hoàn tiền", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberText)
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Thời gian tới buổi tập", color = AmberText, fontSize = 14.sp)
                    Text(timeDiffStr, fontWeight = FontWeight.Bold, color = AmberValue, fontSize = 14.sp)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Tỉ lệ hoàn tiền", color = AmberText, fontSize = 14.sp)
                    Text("$refundPct%", fontWeight = FontWeight.Bold, color = AmberValue, fontSize = 14.sp)
                }
                
                HorizontalDivider(thickness = 0.5.dp, color = AmberBorder)
                
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Bạn sẽ nhận lại", color = AmberText, fontSize = 14.sp)
                    Text(currencyFormat.format(refundAmount), fontWeight = FontWeight.Bold, color = AmberValue, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            val showDialog = remember { mutableStateOf(false) }
            
            Button(
                onClick = { showDialog.value = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF7C1C1))
            ) {
                Text("Xác nhận hủy lịch hẹn", color = RejectText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Xác nhận hủy?") },
                    text = { Text("Lưu ý: Hành động này không thể hoàn tác. Số tiền hoàn lại sẽ dựa trên chính sách hủy của hệ thống.") },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.cancelBooking(reason = "Người dùng yêu cầu hủy qua ứng dụng")
                            showDialog.value = false
                        }) {
                            Text("Đồng ý hủy", color = RejectText, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog.value = false }) {
                            Text("Giữ lịch hẹn", color = Tsecondary)
                        }
                    }
                )
            }

            // Status Messages
            when (val state = uiState) {
                is PtBookingCancelUiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlueActive)
                    }
                }
                is PtBookingCancelUiState.Success -> {
                    LaunchedEffect(Unit) {
                        onNavigateBack()
                    }
                }
                is PtBookingCancelUiState.Error -> {
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                else -> {}
            }
        }
    }
}
