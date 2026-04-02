package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.gymapp.android.domain.model.membership.PlanType
import com.gymapp.android.ui.screens.membership.viewmodel.PackageDetailViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    viewModel: PackageDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết gói", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF5722))
            } else if (!uiState.error.isNullOrEmpty()) {
                Text(uiState.error!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (uiState.plan != null) {
                val plan = uiState.plan!!
                val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                val formattedPrice = "${formatter.format(plan.price.toLong())}đ"

                Column(modifier = Modifier.fillMaxSize()) {
                    Text(text = plan.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "$formattedPrice / ${plan.durationDays} ngày", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFF5722))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        color = if (plan.planType == PlanType.ALL) Color(0xFF1B5E20) else Color(0xFFE65100),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (plan.planType == PlanType.ALL) "Toàn chuỗi" else "1 Chi nhánh: ${plan.branchName ?: ""}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Mô tả", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = plan.description, fontSize = 16.sp, color = Color(0xFF666666))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Quyền lợi nổi bật", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "✔️ Truy cập phòng tập không giới hạn\n✔️ Miễn phí lớp Yoga, Zumba cơ bản\n✔️ Nước uống, locker miễn phí", fontSize = 16.sp, color = Color(0xFF666666), lineHeight = 24.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = { /* Handle Payment via Mock later */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mua ngay - $formattedPrice", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
