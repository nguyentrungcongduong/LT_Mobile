package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.gymapp.android.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PaymentResultScreen(
    bookingId: String,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit, // In case of failure, back to S1
    onCancel: () -> Unit // Cancel pending booking
) {
    // In a real implementation, we would poll the server via ViewModel to check if `status` is CONFIRMED, CANCELLED or PENDING.
    // For this wireframe representation, we'll simulate "Đang xử lý -> Thành công"
    // The spec mentions 3 states: S4a (Success), S4b (Failed), S4c (Pending)
    
    var state by remember { mutableStateOf("PENDING") } // "PENDING", "SUCCESS", "FAILED"

    LaunchedEffect(Unit) {
        // Polling simulation
        delay(2000)
        state = "SUCCESS" // Simulating success hook
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                "PENDING" -> {
                    // S4c Pending
                    Box(modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium).background(Color(0xFFE65100).copy(alpha = 0.3f)).border(2.dp, Color(0xFFFF5722), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                        Text("⏱", fontSize = 24.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Đang xử lý", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Giao dịch đang xác nhận. Có thể mất 1–5 phút. Sẽ thông báo khi xong.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    
                    Spacer(Modifier.height(32.dp))
                    Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                        ResultRow("Booking", bookingId)
                        ResultRow("Trạng thái", "Chờ xác nhận", Color(0xFFFFC107))
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onNavigateHome, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Về Trang Chủ", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                "SUCCESS" -> {
                    // S4a Success
                    Box(modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium).background(Color(0xFF1B5E20).copy(alpha = 0.3f)).border(2.dp, color_success, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                        Text("✓", fontSize = 24.sp, color = color_success)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Thanh toán thành công", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    
                    Spacer(Modifier.height(32.dp))
                    Column(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surface).padding(16.dp)) {
                        ResultRow("Trạng thái", "Đã xác nhận", color_success)
                        ResultRow("Booking ID", bookingId)
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onNavigateHome, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Về Trang Chủ", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                "FAILED" -> {
                    // S4b Failed
                    Box(modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium).background(Color(0xFFB71C1C).copy(alpha = 0.3f)).border(2.dp, MaterialTheme.colorScheme.error, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                        Text("✗", fontSize = 24.sp, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Thanh toán thất bại", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Giao dịch bị từ chối. Kiểm tra thẻ hoặc thử phương thức khác.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    
                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Thử lại với provider khác", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("Hủy lịch hẹn này", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor, maxLines = 1)
    }
}
