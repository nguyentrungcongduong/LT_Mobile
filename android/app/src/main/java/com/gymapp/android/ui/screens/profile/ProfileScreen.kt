package com.gymapp.android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gymapp.android.domain.model.User
import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal
import com.gymapp.android.util.FileUtil

// ---- Light Theme Colors ----
val BgColor = Color(0xFFF8F9FA) // Nền trắng hơi xám nhẹ để nổi bật Card
val CardBackground = Color.White
val PrimaryOrange = Color(0xFFFF5722)
val PrimaryGreen = Color(0xFF4CAF50)
val TextDark = Color(0xFF111827) // Chữ màu đen
val TextGray = Color(0xFF6B7280) // Chữ phụ màu xám
val IconBackground = Color(0xFFF3F4F6) // Nền của các viền icon
val DividerColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = FileUtil.getFileFromUri(context, it)
            if (file != null) {
                viewModel.uploadAvatar(file)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 24.sp) },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(CardBackground, CircleShape)
                            .border(1.dp, DividerColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgColor,
                    titleContentColor = TextDark
                )
            )
        },
        containerColor = BgColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryOrange
                    )
                }
                is ProfileUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = Color(0xFFE53935))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
                is ProfileUiState.Success -> {
                    val user = state.user
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Section
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Orange circle stroke
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(2.dp, PrimaryOrange, CircleShape)
                                    .padding(4.dp)
                            ) {
                                if (user.avatarUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user.avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "${user.fullName}'s avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(IconBackground, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.fullName?.take(1)?.uppercase() ?: "N",
                                            fontSize = 40.sp,
                                            color = PrimaryOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Uploading state or Edit icon
                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = PrimaryOrange,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(BgColor, CircleShape)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(PrimaryOrange, CircleShape)
                                            .clickable { launcher.launch("image/*") }
                                            .padding(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Upload Avatar",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Info
                        Text(
                            text = user.fullName ?: "Chưa cập nhật",
                            color = TextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeItem(user.role, PrimaryOrange)
                            user.experienceLevel?.let { level ->
                                BadgeItem(level.name, PrimaryGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Logic UI theo Role
                        if (user.role == "PT") {
                            // PT View
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D))
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.VerifiedUser, tint = Color(0xFFFFD700), contentDescription = null, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Trainer Chuyên Nghiệp", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("150+", color = Color(0xFFFF5722), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                                Text("Giờ dạy", color = Color.LightGray, fontSize = 13.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("12", color = Color(0xFFFF5722), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                                Text("Học viên", color = Color.LightGray, fontSize = 13.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("4.9", color = Color(0xFFFF5722), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                                    Icon(Icons.Default.Star, tint = Color(0xFFFFD700), contentDescription = null, modifier = Modifier.size(20.dp).padding(start = 2.dp))
                                                }
                                                Text("Đánh giá", color = Color.LightGray, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBackground)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(Color(0xFFFFF3E0), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = PrimaryOrange)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Chuyên môn chính", color = TextGray, fontSize = 13.sp)
                                        Text(text = "Bodybuilding & Fitness", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
                                }
                            }
                        } else if (user.role == "ADMIN") {
                            // Admin View
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBackground, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp).background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF3F51B5))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Quyền hạn", color = TextGray, fontSize = 12.sp)
                                    Text(text = "Toàn quyền quản trị hệ thống", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // User View (Mặc định)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(modifier = Modifier.weight(1f), value = "24", label = "Buổi tập", valueColor = TextDark)
                                StatCard(modifier = Modifier.weight(1f), value = "7", label = "Ngày streak", valueColor = PrimaryOrange)
                                StatCard(modifier = Modifier.weight(1f), value = "36h", label = "Tổng giờ", valueColor = TextDark)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onNavigateToGoal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                            ) {
                                Text(
                                    text = "Cập nhật mục tiêu (Goal)",
                                    color = Color(0xFFFF5722),
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Goal Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBackground, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp).background(Color(0xFFFFF0EC), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdsClick, contentDescription = null, tint = PrimaryOrange)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Mục tiêu hiện tại", color = TextGray, fontSize = 12.sp)
                                    val goalText = when(user.fitnessGoal) {
                                        FitnessGoal.WEIGHT_LOSS -> "Giảm cân & Mỡ"
                                        FitnessGoal.MUSCLE_GAIN -> "Tăng cơ bắp"
                                        FitnessGoal.ENDURANCE -> "Tăng thể lực & Sức bền"
                                        null -> "Chưa thiết lập"
                                    }
                                    Text(text = goalText, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Account Settings Section
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text(
                                "TÀI KHOẢN",
                                color = TextGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBackground, RoundedCornerShape(16.dp))
                            ) {
                                SettingRow(
                                    icon = Icons.Default.Person,
                                    title = "Chỉnh sửa thông tin",
                                    subtitle = "Tên, email, số điện thoại",
                                    onClick = { showEditDialog = true }
                                )
                                HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                SettingRow(
                                    icon = Icons.Default.Lock,
                                    title = "Đổi mật khẩu",
                                    subtitle = "Bảo mật tài khoản",
                                    onClick = { showPasswordDialog = true }
                                )
                                HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                if (user.role == "USER") {
                                    SettingRow(
                                        icon = Icons.Default.List,
                                        title = "Lịch sử giao dịch",
                                        subtitle = "Thanh toán membership, PT",
                                        onClick = onNavigateToHistory
                                    )
                                    HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                }

                                SettingRowItemSwitch(
                                    icon = Icons.Default.Notifications,
                                    title = "Thông báo",
                                    subtitle = "Nhắc tập, booking, hết hạn"
                                )
                                HorizontalDivider(color = DividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                                SettingRow(
                                    icon = Icons.Default.Language,
                                    title = "Ngôn ngữ",
                                    subtitle = "Tiếng Việt",
                                    onClick = {}
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Logout Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardBackground, RoundedCornerShape(16.dp))
                                .clickable { onLogout() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFE53935))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Đăng xuất", color = Color(0xFFE53935), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Edit Dialog
                        if (showEditDialog) {
                            EditProfileDialog(
                                user = user,
                                isUpdating = isUpdating,
                                onDismiss = { showEditDialog = false },
                                onConfirm = { name, email, phone ->
                                    viewModel.updateProfile(name, email, phone)
                                    showEditDialog = false
                                }
                            )
                        }

                        // Password Dialog
                        if (showPasswordDialog) {
                            ChangePasswordDialog(
                                isUpdating = isUpdating,
                                onDismiss = { showPasswordDialog = false },
                                onConfirm = { oldPass, newPass ->
                                    viewModel.changePassword(
                                        oldPass = oldPass,
                                        newPass = newPass,
                                        onSuccess = {
                                            showPasswordDialog = false
                                            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, valueColor: Color) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}

@Composable
fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(IconBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextGray, fontSize = 13.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextGray)
    }
}

@Composable
fun SettingRowItemSwitch(icon: ImageVector, title: String, subtitle: String) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).background(IconBackground, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextGray, fontSize = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryOrange,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = IconBackground,
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: User,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName ?: "") }
    var email by remember { mutableStateOf(user.email ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(text = "Chỉnh sửa thông tin", color = TextDark, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ tên", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                enabled = !isUpdating
            ) {
                Text(if (isUpdating) "Đang lưu..." else "Lưu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextGray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(text = "Đổi mật khẩu", color = TextDark, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Mật khẩu hiện tại", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới", color = TextGray) },
                    singleLine = true,
                    isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgColor,
                        unfocusedContainerColor = BgColor,
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = PrimaryOrange,
                        unfocusedBorderColor = DividerColor,
                        errorBorderColor = Color.Red,
                        errorTextColor = Color.Red
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text("Mật khẩu xác nhận không khớp", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                enabled = !isUpdating && oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword
            ) {
                Text(if (isUpdating) "Đang lưu..." else "Lưu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = TextGray)
            }
        }
    )
}
