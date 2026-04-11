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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val BlueActive     = Color(0xFF185FA5)
private val BlueLight      = Color(0xFFE6F1FB)
private val BlueBorder     = Color(0xFFB5D4F4)
private val BlueDeep      = Color(0xFF042C53)
private val GreenLight     = Color(0xFFEAF3DE)
private val GreenText      = Color(0xFF3B6D11)
private val GreenBorder    = Color(0xFFC0DD97)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PtBookingConfirmScreen(
    onNavigateBack: () -> Unit,
    onBookingSuccess: (String) -> Unit,
    viewModel: PtBookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ptDetail by viewModel.ptDetail.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSlotId by viewModel.selectedSlotId.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt lịch", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                modifier = Modifier.border(0.5.dp, BorderTertiary)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPrimary)
                    .border(0.5.dp, BorderTertiary)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { viewModel.confirmBooking() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueActive),
                        enabled = uiState !is PtBookingUiState.Loading
                    ) {
                        if (uiState is PtBookingUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Thanh toán ngay qua VNPAY", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Đặt lịch tự hủy sau 15 phút nếu chưa thanh toán",
                        fontSize = 12.sp,
                        color = Tsecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
            // PT Info Strip
            ptDetail?.let { pt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSecondary)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pt.avatarUrl.isNullOrBlank()) {
                        AvatarBubble(name = pt.fullName, modifier = Modifier.size(48.dp))
                    } else {
                        AsyncImage(
                            model = pt.avatarUrl,
                            contentDescription = pt.fullName,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(pt.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Tprimary)
                        Text(pt.specializations?.firstOrNull() ?: "Personal Trainer", fontSize = 14.sp, color = Tsecondary)
                    }
                }
            }

            // Booking Confirm Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlueLight)
                    .border(1.dp, BlueBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConfirmRow(label = "Ngày", value = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN")).format(selectedDate))
                
                // For slot time, we would need to find the slot from availabilities if not already stored
                // But for now, we can show a placeholder or pass the slot text.
                // Assuming we can derive it or it's static in this view.
                ConfirmRow(label = "Giờ", value = "Khung giờ đã chọn (60 phút)")

                HorizontalDivider(thickness = 0.5.dp, color = BlueBorder)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng thanh toán", fontSize = 14.sp, color = BlueActive)
                    Text(
                        currencyFormat.format(ptDetail?.price ?: 0.0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueDeep
                    )
                }
                
                Text(
                    "PT nhận: 80% · Phí nền tảng: 20%",
                    fontSize = 11.sp,
                    color = BlueActive,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Refund Policy Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenLight)
                    .border(1.dp, GreenBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Chính sách hoàn tiền", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenText)
                Text("• Hủy sau 24h → hoàn 100%", fontSize = 12.sp, color = GreenText)
                Text("• Hủy trong 24h → hoàn 50%", fontSize = 12.sp, color = GreenText)
                Text("• Hủy trong 2h → không hoàn tiền", fontSize = 12.sp, color = GreenText)
            }
            
            if (uiState is PtBookingUiState.Error) {
                Text(
                    (uiState as PtBookingUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Handle Success
    if (uiState is PtBookingUiState.BookingSuccess) {
        val response = (uiState as PtBookingUiState.BookingSuccess).response
        LaunchedEffect(response) {
            onBookingSuccess(response.paymentUrl ?: "")
        }
    }
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = BlueActive)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.W500, color = BlueDeep)
    }
}
