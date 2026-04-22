package com.gymapp.android.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.gymapp.android.data.remote.api.LoginRequest

// ── Dark Mode Brand Colors ────────────────────────────────────
private val BgDark        = Color(0xFF121212)
private val SurfaceCard   = Color(0xFF1E1E1E)
private val SurfaceInput  = Color(0xFF252525)
private val OrangeMain    = Color(0xFFFF5722)
private val OrangeLight   = Color(0xFFFF7043)
private val OrangeDeep    = Color(0xFFE64A19)
private val OrangeGlow1   = Color(0x40FF5722)   // blob chính ~25%
private val OrangeGlow2   = Color(0x1AFF5722)   // blob phụ ~10%
private val BorderDefault = Color(0xFF2C2C2C)
private val TextPrimary   = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF9E9E9E)
private val TextHint      = Color(0xFF5C5C5C)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authState = viewModel.uiState

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
            viewModel.resetState()
        } else if (authState is AuthState.Error) {
            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // ── Orange Glow Blob — top right ──────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 100.dp, y = (-80).dp)
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(OrangeGlow1, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        // ── Orange Glow Blob — bottom left ───────────────────
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-80).dp, y = 80.dp)
                .align(Alignment.BottomStart)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(OrangeGlow2, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        // ── Small accent blob — center right ─────────────────
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 40.dp, y = 0.dp)
                .align(Alignment.CenterEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(OrangeGlow2, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo ─────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = OrangeMain,
                                spotColor = OrangeMain
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(OrangeDeep, OrangeLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💪", fontSize = 34.sp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "GYM APP",
                        color = OrangeMain,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đăng Nhập",
                        color = TextPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Chào mừng trở lại, chiến binh! 🔥",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── Email Field ──────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 30 }
            ) {
                Column {
                    Text(
                        text = "EMAIL",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceInput)
                            .border(
                                width = if (emailFocused) 1.5.dp else 1.dp,
                                color = if (emailFocused) OrangeMain else BorderDefault,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("example@email.com", color = TextHint, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Email, null,
                                    tint = if (emailFocused) OrangeMain else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = OrangeMain
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { emailFocused = it.isFocused }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Password Field ───────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 180)) + slideInVertically(tween(600, 180)) { 30 }
            ) {
                Column {
                    Text(
                        text = "MẬT KHẨU",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceInput)
                            .border(
                                width = if (passwordFocused) 1.5.dp else 1.dp,
                                color = if (passwordFocused) OrangeMain else BorderDefault,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = TextHint, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Lock, null,
                                    tint = if (passwordFocused) OrangeMain else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = if (passwordFocused) OrangeMain else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = OrangeMain
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { passwordFocused = it.isFocused }
                        )
                    }
                }
            }

            // ── Error ────────────────────────────────────────
            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠ ${authState.message}",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Login Button ─────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, 260)) + slideInVertically(tween(600, 260)) { 30 }
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
                            ambientColor = OrangeMain,
                            spotColor = OrangeMain
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (authState !is AuthState.Loading)
                                    listOf(OrangeDeep, OrangeLight)
                                else listOf(Color(0xFF6B3020), Color(0xFF6B3020))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.login(LoginRequest(email, password)) },
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
                                text = "ĐĂNG NHẬP",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDefault)
                Text("  hoặc  ", color = TextSecondary, fontSize = 12.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDefault)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Register Link ────────────────────────────────
            TextButton(onClick = onNavigateToRegister) {
                Text("Chưa có tài khoản? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Đăng ký ngay",
                    color = OrangeMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
