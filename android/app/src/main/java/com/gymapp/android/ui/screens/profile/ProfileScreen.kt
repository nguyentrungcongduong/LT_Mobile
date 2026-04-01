package com.gymapp.android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gymapp.android.domain.model.User
import com.gymapp.android.util.FileUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

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
                title = { Text("Hồ sơ", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D0D),
                    titleContentColor = Color(0xFFEDEDEC)
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFF5722)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
                is ProfileUiState.Success -> {
                    val user = state.user
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Section
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242424))
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatarUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user.avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "${user.fullName}'s avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    tint = Color(0xFF8A8F98),
                                    modifier = Modifier.size(64.dp)
                                )
                            }

                            if (isUploading) {
                                CircularProgressIndicator(
                                    color = Color(0xFFFF5722),
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xFFFF5722), CircleShape)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Upload Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // User Info
                        Text(
                            text = user.fullName,
                            color = Color(0xFFEDEDEC),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user.email,
                            color = Color(0xFF8A8F98),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AssistChip(
                            onClick = { },
                            label = { Text(user.role, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF1B5E20).copy(alpha = 0.3f)
                            ),
                            border = AssistChipDefaults.assistChipBorder(borderColor = Color.Transparent)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Actions
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFEDEDEC))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Chỉnh sửa thông tin", color = Color(0xFFEDEDEC), fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFE53935))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Đăng xuất", color = Color(0xFFE53935), fontSize = 16.sp)
                        }

                        // Edit Dialog
                        if (showEditDialog) {
                            EditProfileDialog(
                                user = user,
                                isUpdating = isUpdating,
                                onDismiss = { showEditDialog = false },
                                onConfirm = { name, phone ->
                                    viewModel.updateProfile(name, phone)
                                    showEditDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    user: User,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf("") } // Replace with user.phone if phone is added to model

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(text = "Chỉnh sửa thông tin", color = Color(0xFFEDEDEC), fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ tên", color = Color(0xFF8A8F98)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF242424),
                        unfocusedContainerColor = Color(0xFF242424),
                        focusedTextColor = Color(0xFFEDEDEC),
                        unfocusedTextColor = Color(0xFFEDEDEC),
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF2E2E2E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại", color = Color(0xFF8A8F98)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF242424),
                        unfocusedContainerColor = Color(0xFF242424),
                        focusedTextColor = Color(0xFFEDEDEC),
                        unfocusedTextColor = Color(0xFFEDEDEC),
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF2E2E2E)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                enabled = !isUpdating
            ) {
                Text(if (isUpdating) "Đang lưu..." else "Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color(0xFF8A8F98))
            }
        }
    )
}
