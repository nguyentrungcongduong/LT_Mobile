package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.domain.model.membership.PlanType
import com.gymapp.android.ui.screens.membership.viewmodel.PackageDetailViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.NumberFormat
import java.util.*
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageDetailScreen(
    viewModel: PackageDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (encodedUrl: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.gatewayUrl) {
        uiState.gatewayUrl?.let { url ->
            // Encode URL để truyền an toàn qua NavArgs
            val encodedUrl = Base64.encodeToString(
                url.toByteArray(),
                Base64.URL_SAFE or Base64.NO_WRAP
            )
            onNavigateToPayment(encodedUrl)
            viewModel.clearGatewayUrl()
        }
    }

    // Theo chuẩn ui_standar.md (Modern Dark + Bold Typography)
    val bgColor = Color(0xFF0D0D0D)
    val surfaceColor = Color(0xFF1A1A1A)
    val surfaceVariant = Color(0xFF242424)
    val primaryColor = Color(0xFFFF5722)
    val primaryContainer = Color(0xFF3D1A0A)
    val onPrimaryContainer = Color(0xFFFFCCBC)
    val textColor = Color(0xFFEDEDEC)
    val textMuted = Color(0xFF8A8F98)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết gói", color = textColor, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor,
        bottomBar = {
            if (uiState.plan != null) {
                val plan = uiState.plan!!
                val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                val formattedPrice = "${formatter.format(plan.price.toLong())}đ"
                
                Surface(
                    color = surfaceColor,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val btnText = if (uiState.isCurrentUserPlan) "Gia hạn ngay - $formattedPrice" else "Đăng ký ngay - $formattedPrice"
                                Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                
                                val descText = if (uiState.isCurrentUserPlan && uiState.activeUntil != null) {
                                    "Gói hiện tại sẽ hết hạn vào ${uiState.activeUntil}"
                                } else {
                                    "Chỉ khoảng ~${formatter.format(plan.price.toLong() / plan.durationDays)}đ/ngày"
                                }
                                Text(descText, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else if (!uiState.error.isNullOrEmpty()) {
                Text(uiState.error!!, color = Color(0xFFE53935), modifier = Modifier.align(Alignment.Center))
            } else if (uiState.plan != null) {
                val plan = uiState.plan!!
                val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
                val formattedPrice = "${formatter.format(plan.price.toLong())}đ"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    // Fallback toạ độ dựa theo Tên gói hoặc Thời hạn chốt theo yêu cầu
                    val defaultLat = when {
                        plan.name.contains("12") || plan.durationDays >= 360 -> 10.797037057855999
                        plan.name.contains("3") || plan.durationDays == 90 -> 10.790856210161808
                        else -> 10.776354032266614 // Gói 1 tháng hoặc mặc định
                    }
                    val defaultLng = when {
                        plan.name.contains("12") || plan.durationDays >= 360 -> 106.70274181968595
                        plan.name.contains("3") || plan.durationDays == 90 -> 106.69007773022825
                        else -> 106.69346708476218
                    }

                    // Interactive Google Map 
                    val branchLocation = LatLng(plan.branchLatitude ?: defaultLat, plan.branchLongitude ?: defaultLng)
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(branchLocation, 15f)
                    }

                    // Tự động căn chỉnh map nếu gói là ALL
                    LaunchedEffect(plan.availableBranches) {
                        if (plan.planType == PlanType.ALL && plan.availableBranches.isNotEmpty()) {
                            val builder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                            plan.availableBranches.forEach {
                                builder.include(LatLng(it.latitude, it.longitude))
                            }
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(builder.build(), 100)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            if (plan.planType == PlanType.ALL) {
                                plan.availableBranches.forEach { branch ->
                                    Marker(
                                        state = MarkerState(position = LatLng(branch.latitude, branch.longitude)),
                                        title = branch.name
                                    )
                                }
                            } else {
                                Marker(
                                    state = MarkerState(position = branchLocation),
                                    title = plan.branchName ?: "Chi nhánh hỗ trợ"
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tag "Phổ biến nhất" 
                        Surface(
                            color = primaryContainer,
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                "🔥 Phổ biến nhất",
                                color = onPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Tên gói & Badge trạng thái
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = plan.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                            
                            if (uiState.isCurrentUserPlan && !uiState.isExpired) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF1B5E20).copy(alpha = 0.3f), // ui_standar ACTIVE bg
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "Đang sử dụng",
                                        color = Color(0xFF4CAF50), // ui_standar ACTIVE text
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else if (uiState.isExpired) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF212121), // ui_standar EXPIRED bg
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "Đã hết hạn",
                                        color = Color(0xFF8A8F98), // ui_standar EXPIRED text
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Giá tiền 
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(text = formattedPrice, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Text(text = " / ${plan.durationDays} ngày", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textMuted, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Thẻ chi nhánh bo tròn
                        Surface(
                            color = primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(percent = 50),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (plan.planType == PlanType.ALL) "📍 Toàn chuỗi" else "📍 1 Chi nhánh: ${plan.branchName ?: ""}",
                                color = primaryColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Thêm ngày hết hạn nếu đang có gói (Cách 1)
                        if (uiState.isCurrentUserPlan && uiState.activeUntil != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hết hạn vào: ${uiState.activeUntil}",
                                color = textMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Mô tả", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = plan.description, fontSize = 14.sp, color = textMuted, lineHeight = 20.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Quyền lợi nổi bật 
                        Text(text = "Quyền lợi nổi bật", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                BenefitItem(icon = Icons.Default.LocationOn, text = "Truy cập phòng tập không giới hạn không gian và thời gian", primaryColor = primaryColor, textMuted = textMuted)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = surfaceVariant)
                                BenefitItem(icon = Icons.Default.Face, text = "Miễn phí lớp theo nhóm: Yoga, Zumba cơ bản", primaryColor = primaryColor, textMuted = textMuted)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = surfaceVariant)
                                BenefitItem(icon = Icons.Default.Star, text = "Nước uống, tủ để đồ locker thông minh miễn phí", primaryColor = primaryColor, textMuted = textMuted)
                            }
                        }

                        // Box Review
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(text = "Đánh giá từ hội viên", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            color = surfaceColor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("5.0", color = textColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("\"Gói cơ bản nhưng cực kỳ đầy đủ, phù hợp cho người mới bắt đầu như mình.\"", color = textMuted, fontSize = 14.sp, fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog && uiState.plan != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Chọn phương thức thanh toán", color = textColor) },
            text = {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            showPaymentDialog = false
                            viewModel.initiatePayment("VNPAY", uiState.plan!!.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005BAA))
                    ) {
                        Text("Thanh toán qua VNPAY", color = Color.White)
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = {
                            showPaymentDialog = false
                            viewModel.initiatePayment("MOMO", uiState.plan!!.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA50064))
                    ) {
                        Text("Thanh toán qua MoMo", color = Color.White)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Hủy", color = textMuted)
                }
            },
            containerColor = surfaceColor
        )
    }
}

@Composable
fun BenefitItem(icon: ImageVector, text: String, primaryColor: Color, textMuted: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = primaryColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 14.sp, color = textMuted, lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
