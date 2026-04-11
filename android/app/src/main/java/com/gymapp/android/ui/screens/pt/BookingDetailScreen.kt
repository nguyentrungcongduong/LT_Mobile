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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.BookingDto
import com.gymapp.android.data.remote.api.CancelBookingRequest

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val RejectBg       = Color(0xFFFCEBEB)
private val RejectText     = Color(0xFFA32D2D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    viewModel: PtBookingViewModel = hiltViewModel() // Reuse booking VM or create special detail VM
) {
    // For simplicity, we'll assume we have the booking data or fetch it.
    // In a real app, I'd create a specific BookingDetailViewModel.
    
    var showCancelDialog by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(BgPrimary).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder for booking info display
            Text("Mã đơn: #$bookingId", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(12.dp))

            // Cancel Button (only if status is allowed, here we just show it for demo)
            Button(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RejectBg, contentColor = RejectText)
            ) {
                Text("Hủy lịch hẹn", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Xác nhận hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn hủy lịch hẹn này không? Tiền sẽ được hoàn trả theo chính sách.", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { 
                    // Call API cancel
                    onNavigateBack()
                }) {
                    Text("Hủy lịch", color = RejectText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Quay lại")
                }
            }
        )
    }
}
