package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.gymapp.android.ui.components.cards.ActiveMembershipCard
import com.gymapp.android.ui.screens.membership.event.MembershipDetailEvent
import com.gymapp.android.ui.screens.membership.state.MembershipDetailUiState
import com.gymapp.android.ui.screens.membership.viewmodel.MembershipDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveMembershipScreen(
    viewModel: MembershipDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPackages: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hội viên của tôi", color = Color(0xFF1A1A1A), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở lại", tint = Color(0xFF1A1A1A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState) {
                is MembershipDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFF5722)
                    )
                }

                is MembershipDetailUiState.Empty -> {
                    EmptyMembershipState(onNavigateToPackages = onNavigateToPackages)
                }

                is MembershipDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as MembershipDetailUiState.Error).message, 
                            color = Color(0xFFE53935)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onEvent(MembershipDetailEvent.Refresh) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                        ) {
                            Text("Thử lại")
                        }
                    }
                }

                is MembershipDetailUiState.Success -> {
                    val membership = (uiState as MembershipDetailUiState.Success).activeMembership
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        ActiveMembershipCard(membership = membership)

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onEvent(MembershipDetailEvent.OnShowQrClicked) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A1A))
                            ) {
                                Text("🔳 Hiện mã QR", fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = onNavigateToPackages,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5722),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("🔄 Gia hạn", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMembershipState(onNavigateToPackages: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎫", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có gói hội viên",
            color = Color(0xFF1A1A1A),
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mua gói để vào tập ngay!",
            color = Color(0xFF666666),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToPackages,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Xem các gói hội viên", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
