package com.gymapp.android.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.RegisterRequest

// ── Dark Mode Brand Colors (đồng bộ với LoginScreen) ─────────
private val RegBgDark       = Color(0xFF121212)
private val RegSurfaceInput = Color(0xFF252525)
private val RegOrangeMain   = Color(0xFFFF5722)
private val RegOrangeLight  = Color(0xFFFF7043)
private val RegOrangeDeep   = Color(0xFFE64A19)
private val RegOrangeGlow1  = Color(0x40FF5722)
private val RegOrangeGlow2  = Color(0x1AFF5722)
private val RegBorderDefault= Color(0xFF2C2C2C)
private val RegTextPrimary  = Color(0xFFF5F5F5)
private val RegTextSecondary= Color(0xFF9E9E9E)
private val RegTextHint     = Color(0xFF5C5C5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (isPt: Boolean) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordConfirmVisible by remember { mutableStateOf(false) }
    var isPtRole by remember { mutableStateOf(false) }

    var fullNameFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var phoneFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var confirmFocused by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState = viewModel.uiState

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Toast.makeText(context, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show()
            onRegisterSuccess(isPtRole)
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RegBgDark)
    ) {
        // ── Orange Glow Blob — top right ──────────────────────
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 90.dp, y = (-70).dp)
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RegOrangeGlow1, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        // ── Orange Glow Blob — bottom left ───────────────────
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 60.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RegOrangeGlow2, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        // ── Small blob — center left accent ──────────────────
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-30).dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RegOrangeGlow2, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(72.dp))

            // ── Header ───────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = RegOrangeMain,
                                spotColor = RegOrangeMain
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(RegOrangeDeep, RegOrangeLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏋️", fontSize = 30.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GYM APP",
                        color = RegOrangeMain,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tạo Tài Khoản",
                        color = RegTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bắt đầu hành trình luyện tập của bạn 💪",
                        color = RegTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Reusable styled field ─────────────────────────
            @Composable
            fun DarkField(
                value: String,
                onValueChange: (String) -> Unit,
                label: String,
                placeholder: String,
                isFocused: Boolean,
                onFocusChange: (Boolean) -> Unit,
                leadingIcon: @Composable () -> Unit,
                trailingIcon: (@Composable () -> Unit)? = null,
                keyboardType: KeyboardType = KeyboardType.Text,
                visualTransformation: VisualTransformation = VisualTransformation.None,
                delayMs: Int = 0
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(600, delayMs)) + slideInVertically(tween(600, delayMs)) { 30 }
                ) {
                    Column {
                        Text(
                            text = label,
                            color = RegTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(RegSurfaceInput)
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) RegOrangeMain else RegBorderDefault,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            TextField(
                                value = value,
                                onValueChange = onValueChange,
                                placeholder = { Text(placeholder, color = RegTextHint, fontSize = 14.sp) },
                                leadingIcon = leadingIcon,
                                trailingIcon = trailingIcon,
                                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                                visualTransformation = visualTransformation,
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = RegTextPrimary,
                                    unfocusedTextColor = RegTextPrimary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = RegOrangeMain
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { onFocusChange(it.isFocused) }
                            )
                        }
                    }
                }
            }

            // ── Họ và Tên ─────────────────────────────────────
            DarkField(
                value = fullName, onValueChange = { fullName = it },
                label = "HỌ VÀ TÊN", placeholder = "Nguyễn Văn A",
                isFocused = fullNameFocused, onFocusChange = { fullNameFocused = it },
                leadingIcon = {
                    Icon(Icons.Filled.Person, null,
                        tint = if (fullNameFocused) RegOrangeMain else RegTextSecondary,
                        modifier = Modifier.size(20.dp))
                },
                delayMs = 80
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ── Email ─────────────────────────────────────────
            DarkField(
                value = email, onValueChange = { email = it },
                label = "EMAIL", placeholder = "example@email.com",
                isFocused = emailFocused, onFocusChange = { emailFocused = it },
                leadingIcon = {
                    Icon(Icons.Filled.Email, null,
                        tint = if (emailFocused) RegOrangeMain else RegTextSecondary,
                        modifier = Modifier.size(20.dp))
                },
                keyboardType = KeyboardType.Email, delayMs = 140
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ── Số điện thoại ─────────────────────────────────
            DarkField(
                value = phone, onValueChange = { phone = it },
                label = "SỐ ĐIỆN THOẠI", placeholder = "0901 234 567",
                isFocused = phoneFocused, onFocusChange = { phoneFocused = it },
                leadingIcon = {
                    Icon(Icons.Filled.Phone, null,
                        tint = if (phoneFocused) RegOrangeMain else RegTextSecondary,
                        modifier = Modifier.size(20.dp))
                },
                keyboardType = KeyboardType.Phone, delayMs = 200
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ── Mật khẩu ─────────────────────────────────────
            DarkField(
                value = password, onValueChange = { password = it },
                label = "MẬT KHẨU", placeholder = "••••••••",
                isFocused = passwordFocused, onFocusChange = { passwordFocused = it },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, null,
                        tint = if (passwordFocused) RegOrangeMain else RegTextSecondary,
                        modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            null,
                            tint = if (passwordFocused) RegOrangeMain else RegTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                delayMs = 260
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ── Xác nhận mật khẩu ────────────────────────────
            DarkField(
                value = passwordConfirm, onValueChange = { passwordConfirm = it },
                label = "XÁC NHẬN MẬT KHẨU", placeholder = "••••••••",
                isFocused = confirmFocused, onFocusChange = { confirmFocused = it },
                leadingIcon = {
                    Icon(Icons.Filled.LockOpen, null,
                        tint = if (confirmFocused) RegOrangeMain else RegTextSecondary,
                        modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    IconButton(onClick = { passwordConfirmVisible = !passwordConfirmVisible }) {
                        Icon(
                            if (passwordConfirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            null,
                            tint = if (confirmFocused) RegOrangeMain else RegTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                delayMs = 320
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── PT Checkbox Card ──────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 380))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isPtRole) Color(0x26FF5722) else RegSurfaceInput
                        )
                        .border(
                            width = if (isPtRole) 1.5.dp else 1.dp,
                            color = if (isPtRole) RegOrangeMain else RegBorderDefault,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isPtRole,
                            onCheckedChange = { isPtRole = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RegOrangeMain,
                                uncheckedColor = RegTextSecondary,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Đăng ký làm Personal Trainer (PT)",
                                color = if (isPtRole) RegOrangeMain else RegTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (isPtRole) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                text = "Tài khoản sẽ cần được duyệt trước khi hoạt động",
                                color = RegTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // ── Error ─────────────────────────────────────────
            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠ ${authState.message}",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Register Button ───────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 440)) + slideInVertically(tween(600, 440)) { 30 }
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (authState is AuthState.Loading) 0.97f else 1f,
                    label = "btnScale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .height(56.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = RegOrangeMain,
                            spotColor = RegOrangeMain
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (authState !is AuthState.Loading)
                                    listOf(RegOrangeDeep, RegOrangeLight)
                                else listOf(Color(0xFF6B3020), Color(0xFF6B3020))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val role = if (isPtRole) "PT" else "USER"
                            viewModel.register(
                                RegisterRequest(email, password, fullName, phone, role),
                                passwordConfirm
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        enabled = authState !is AuthState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = "TẠO TÀI KHOẢN",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Divider ───────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = RegBorderDefault)
                Text("  hoặc  ", color = RegTextSecondary, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = RegBorderDefault)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Back to Login ─────────────────────────────────
            TextButton(onClick = onNavigateBack) {
                Text("Đã có tài khoản? ", color = RegTextSecondary, fontSize = 14.sp)
                Text(
                    text = "Đăng nhập ngay",
                    color = RegOrangeMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
