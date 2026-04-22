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
import androidx.compose.ui.graphics.Brush
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
import com.gymapp.android.data.remote.dto.checkin.CheckinStatsResponse
import com.gymapp.android.util.FileUtil

// ──────────────────────────────────────────────────────────────────────────────
// Dark Design Tokens
// ──────────────────────────────────────────────────────────────────────────────
val BgColor        = Color(0xFF121212)
val CardBackground = Color(0xFF1C1C1E)
val BgSurface2     = Color(0xFF252528)
val PrimaryOrange  = Color(0xFFFF6B2B)
val OrangeGlow     = Color(0xFFFF8C00)
val PrimaryGreen   = Color(0xFF2ECC8E)
val TextDark       = Color(0xFFF2F2F2)   // was black → now bright white
val TextGray       = Color(0xFF9A9A9E)
val IconBackground = Color(0xFF2A2A2E)
val DividerColor   = Color(0xFF2A2A2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    onNavigateToGoal: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToWorkoutSchedule: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val workoutStats by viewModel.workoutStats.collectAsState()
    val ptProfile by viewModel.ptProfile.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPtProfileDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = FileUtil.getFileFromUri(context, it)
            if (file != null) viewModel.uploadAvatar(file)
        }
    }

    Scaffold(
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
                        Text(state.message, color = Color(0xFFEF5350))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) { Text("Thử lại", color = Color.White) }
                    }
                }
                is ProfileUiState.Success -> {
                    val user = state.user
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ── Hero Banner ─────────────────────────────────────────
                        ProfileHeroBanner(
                            user = user,
                            isUploading = isUploading,
                            onUploadAvatar = { launcher.launch("image/*") }
                        )

                        // ── Rest of content ─────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                        // ── Name & Email ───────────────────────────────────────
                        Text(
                            text = user.fullName ?: "Chưa cập nhật",
                            color = TextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = user.email,
                            color = TextGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ── Role & Level Badges ────────────────────────────────
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeItem(user.role, PrimaryOrange)
                            user.experienceLevel?.let { level ->
                                BadgeItem(level.name, PrimaryGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Role-specific content ──────────────────────────────
                        when (user.role) {
                            "PT" -> PtRoleSection(user = user, ptProfile = ptProfile, onShowPtProfile = { showPtProfileDialog = true })
                            "ADMIN" -> AdminRoleSection()
                            else -> UserRoleSection(user = user, stats = workoutStats, onNavigateToGoal = onNavigateToGoal)
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Account Settings ───────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            Text(
                                "TÀI KHOẢN",
                                color = TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CardBackground, RoundedCornerShape(16.dp))
                                    .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                            ) {
                                SettingRow(
                                    icon = Icons.Default.Person,
                                    title = "Chỉnh sửa thông tin",
                                    subtitle = "Tên, email, số điện thoại",
                                    onClick = { showEditDialog = true }
                                )
                                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                SettingRow(
                                    icon = Icons.Default.Lock,
                                    title = "Đổi mật khẩu",
                                    subtitle = "Bảo mật tài khoản",
                                    onClick = { showPasswordDialog = true }
                                )
                                if (user.role == "PT") {
                                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                    SettingRow(
                                        icon = Icons.Default.AttachMoney,
                                        title = "Cập nhật hồ sơ PT",
                                        subtitle = "Giá/buổi, kinh nghiệm, bio",
                                        onClick = { showPtProfileDialog = true }
                                    )
                                }
                                if (user.role == "USER") {
                                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                    SettingRow(
                                        icon = Icons.Default.List,
                                        title = "Lịch sử giao dịch",
                                        subtitle = "Thanh toán membership, PT",
                                        onClick = onNavigateToHistory
                                    )
                                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                    SettingRow(
                                        icon = Icons.Default.FitnessCenter,
                                        title = "Lịch tập hàng tuần",
                                        subtitle = "Chọn ngày & giờ nhắc tập",
                                        onClick = onNavigateToWorkoutSchedule
                                    )
                                }
                                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                SettingRowItemSwitch(
                                    icon = Icons.Default.Notifications,
                                    title = "Thông báo",
                                    subtitle = "Nhắc tập, booking, hết hạn"
                                )
                                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                                SettingRow(
                                    icon = Icons.Default.Language,
                                    title = "Ngôn ngữ",
                                    subtitle = "Tiếng Việt",
                                    onClick = {}
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Logout ─────────────────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2A0E0E), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFF4A1515), RoundedCornerShape(16.dp))
                                .clickable { onLogout() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF3D1010), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFEF5350))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Đăng xuất", color = Color(0xFFEF5350), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Dialogs ────────────────────────────────────────────
                        if (showPtProfileDialog) {
                            UpdatePtProfileDialog(
                                isUpdating = isUpdating,
                                onDismiss = { showPtProfileDialog = false },
                                onConfirm = { price, bio, exp ->
                                    viewModel.updatePtProfile(
                                        pricePerSession = price,
                                        bio = bio,
                                        yearsExperience = exp,
                                        onSuccess = {
                                            showPtProfileDialog = false
                                            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                    )
                                }
                            )
                        }
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
                                        onError = { errorMsg -> Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show() }
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
}

// ── Profile Hero Banner ─────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroBanner(
    user: User,
    isUploading: Boolean,
    onUploadAvatar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A0A00),
                            Color(0xFF2A1508),
                            Color(0xFF1C1C1E),
                            Color(0xFF121212)
                        )
                    )
                )
        )

        // Decorative orbs
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(listOf(OrangeGlow.copy(alpha = 0.25f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-20).dp)
                .background(
                    Brush.radialGradient(listOf(PrimaryOrange.copy(alpha = 0.15f), Color.Transparent)),
                    CircleShape
                )
        )

        // "Hồ sơ" title row at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hồ sơ",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF2A2A2E), CircleShape)
                    .border(1.dp, DividerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, null, tint = TextGray, modifier = Modifier.size(18.dp))
            }
        }

        // Avatar centered in banner
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 44.dp)
                .size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(OrangeGlow, PrimaryOrange)),
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                if (user.avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.avatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF2A1508), PrimaryOrange.copy(alpha = 0.4f))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName?.take(1)?.uppercase() ?: "N",
                            fontSize = 36.sp,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            if (isUploading) {
                CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.align(Alignment.Center))
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(BgColor, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryOrange, CircleShape)
                            .clickable { onUploadAvatar() }
                            .padding(5.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }

    // Spacing to account for avatar overflow
    Spacer(modifier = Modifier.height(52.dp))
}

// ── PT Role Section ─────────────────────────────────────────────────────────────
@Composable
private fun PtRoleSection(
    user: User,
    ptProfile: com.gymapp.android.data.remote.api.PtMyProfileDto?,
    onShowPtProfile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Stats card with gradient
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF1E1E28), Color(0xFF2D2D3D)))
                    )
                    .border(1.dp, Color(0xFF3A3A4E), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, tint = Color(0xFFFFD700), contentDescription = null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Trainer Chuyên Nghiệp", color = TextDark, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val sessions = ptProfile?.totalSessions?.toString() ?: "–"
                        val clients  = ptProfile?.totalClients?.toString() ?: "–"
                        val rating   = ptProfile?.ratingAvg?.let {
                            if (it == 0.0) "–" else "$it ★"
                        } ?: "–"
                        PtStatItem(sessions, "Giờ dạy")
                        PtStatItem(clients,  "Học viên")
                        PtStatItem(rating,   "Đánh giá")
                    }
                }
            }
        }

        // Specialty card
        val mainSpecialty = ptProfile?.specializations?.firstOrNull() ?: "Chưa cập nhật"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                .clickable { onShowPtProfile() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFF2A1508), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, null, tint = PrimaryOrange)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Chuyên môn chính", color = TextGray, fontSize = 13.sp)
                Text(mainSpecialty, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray)
        }
    }
}

@Composable
private fun PtStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = PrimaryOrange, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = TextGray, fontSize = 12.sp)
    }
}

// ── Admin Role Section ─────────────────────────────────────────────────────────
@Composable
private fun AdminRoleSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(16.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(Color(0xFF1A1A3A), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFF7C8FF4))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Quyền hạn", color = TextGray, fontSize = 12.sp)
            Text("Toàn quyền quản trị hệ thống", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── User Role Section ──────────────────────────────────────────────────────────
@Composable
private fun UserRoleSection(user: User, stats: CheckinStatsResponse?, onNavigateToGoal: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Stat cards — fetch từ backend /api/v1/checkin/stats
        val sessions = stats?.totalSessions?.toString() ?: "–"
        val streak   = stats?.streakDays?.toString() ?: "–"
        val hours    = if (stats != null) {
            if (stats.totalHours % 1.0 == 0.0) "${stats.totalHours.toInt()}h" else "${stats.totalHours}h"
        } else "–"
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(modifier = Modifier.weight(1f), value = sessions, label = "Buổi tập", valueColor = TextDark)
            StatCard(modifier = Modifier.weight(1f), value = streak, label = "Ngày streak", valueColor = PrimaryOrange)
            StatCard(modifier = Modifier.weight(1f), value = hours, label = "Tổng giờ", valueColor = TextDark)
        }

        // Goal button (gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF2A1508), Color(0xFF1C1C1E))))
                .border(1.dp, PrimaryOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable { onNavigateToGoal() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Flag, null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                Text("Cập nhật mục tiêu (Goal)", color = PrimaryOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Goal card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(16.dp))
                .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFF2A1508), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AdsClick, null, tint = PrimaryOrange)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Mục tiêu hiện tại", color = TextGray, fontSize = 12.sp)
                val goalText = when (user.fitnessGoal) {
                    FitnessGoal.WEIGHT_LOSS  -> "Giảm cân & Mỡ"
                    FitnessGoal.MUSCLE_GAIN  -> "Tăng cơ bắp"
                    FitnessGoal.ENDURANCE    -> "Tăng thể lực & Sức bền"
                    null                     -> "Chưa thiết lập"
                }
                Text(goalText, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray)
        }
    }
}

// ── Shared Composables ─────────────────────────────────────────────────────────
@Composable
fun BadgeItem(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text = text.uppercase(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, valueColor: Color) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(14.dp))
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = valueColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
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
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextGray, fontSize = 13.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextGray)
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
            Icon(icon, null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextGray, fontSize = 13.sp)
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

// ── Dialogs ────────────────────────────────────────────────────────────────────
private val DialogBg = Color(0xFF1C1C1E)
private val FieldBg  = Color(0xFF252528)

@Composable
private fun darkTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = FieldBg,
    unfocusedContainerColor = FieldBg,
    focusedTextColor        = TextDark,
    unfocusedTextColor      = TextDark,
    focusedBorderColor      = PrimaryOrange,
    unfocusedBorderColor    = DividerColor,
    focusedLabelColor       = PrimaryOrange,
    unfocusedLabelColor     = TextGray,
    cursorColor             = PrimaryOrange
)

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
        containerColor = DialogBg,
        title = { Text("Chỉnh sửa thông tin", color = TextDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Họ tên") }, singleLine = true,
                    colors = darkTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true,
                    colors = darkTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Số điện thoại") }, singleLine = true,
                    colors = darkTextFieldColors(), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                enabled = !isUpdating
            ) { Text(if (isUpdating) "Đang lưu..." else "Lưu", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextGray) }
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
        containerColor = DialogBg,
        title = { Text("Đổi mật khẩu", color = TextDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(value = oldPassword, onValueChange = { oldPassword = it },
                    label = { Text("Mật khẩu hiện tại") }, singleLine = true,
                    colors = darkTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới") }, singleLine = true,
                    colors = darkTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") }, singleLine = true,
                    isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = FieldBg,
                        unfocusedContainerColor = FieldBg,
                        focusedTextColor        = TextDark,
                        unfocusedTextColor      = TextDark,
                        focusedBorderColor      = PrimaryOrange,
                        unfocusedBorderColor    = DividerColor,
                        focusedLabelColor       = PrimaryOrange,
                        unfocusedLabelColor     = TextGray,
                        cursorColor             = PrimaryOrange,
                        errorBorderColor        = Color(0xFFEF5350),
                        errorTextColor          = Color(0xFFEF5350)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text("Mật khẩu xác nhận không khớp", color = Color(0xFFEF5350), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(oldPassword, newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                enabled = !isUpdating && oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword
            ) { Text(if (isUpdating) "Đang lưu..." else "Lưu", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextGray) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePtProfileDialog(
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long?, String?, Int?) -> Unit
) {
    var priceText by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var yearsExpText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DialogBg,
        title = { Text("Cập nhật hồ sơ PT", color = TextDark, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                    label = { Text("Giá/buổi (VNĐ)") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = darkTextFieldColors(),
                    placeholder = { Text("VD: 300000", color = TextGray) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = yearsExpText,
                    onValueChange = { yearsExpText = it.filter { c -> c.isDigit() } },
                    label = { Text("Số năm kinh nghiệm") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = darkTextFieldColors(),
                    placeholder = { Text("VD: 3", color = TextGray) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Giới thiệu bản thân") },
                    maxLines = 3,
                    colors = darkTextFieldColors(),
                    placeholder = { Text("Mô tả kinh nghiệm, chuyên môn...", color = TextGray) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull()
                    val exp = yearsExpText.toIntOrNull()
                    onConfirm(price, bio.ifBlank { null }, exp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                enabled = !isUpdating && (priceText.isNotBlank() || bio.isNotBlank() || yearsExpText.isNotBlank())
            ) { Text(if (isUpdating) "Đang lưu..." else "Lưu", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextGray) }
        }
    )
}
