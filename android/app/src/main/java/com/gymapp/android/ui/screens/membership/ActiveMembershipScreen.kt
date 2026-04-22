package com.gymapp.android.ui.screens.membership

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.MembershipStatus
import com.gymapp.android.ui.components.cards.ActiveMembershipCard
import com.gymapp.android.ui.screens.membership.event.MembershipDetailEvent
import com.gymapp.android.ui.screens.membership.state.MembershipDetailUiState
import com.gymapp.android.ui.screens.membership.viewmodel.MembershipDetailViewModel

// ── Dark Theme Tokens ──────────────────────────────────────────────────────────
private val BgDark       = Color(0xFF121212)
private val BgSurface    = Color(0xFF1C1C1E)
private val BgSurface2   = Color(0xFF252528)
private val BorderDark   = Color(0xFF2A2A2E)
private val TextPrimary  = Color(0xFFF2F2F2)
private val TextSecond   = Color(0xFF9A9A9E)
private val OrangePrim   = Color(0xFFFF6B2B)
private val OrangeLight  = Color(0xFFFF8C00)
private val ErrorRed     = Color(0xFFE53935)
private val SuccessGreen = Color(0xFF43A047)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveMembershipScreen(
    viewModel: MembershipDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPackages: () -> Unit,
    onNavigateToQrDisplay: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hội viên của tôi",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Trở lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BgDark
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is MembershipDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangePrim
                    )
                }

                is MembershipDetailUiState.Empty -> {
                    EmptyMembershipState(onNavigateToPackages = onNavigateToPackages)
                }

                is MembershipDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                        Text(
                            text = (uiState as MembershipDetailUiState.Error).message,
                            color = ErrorRed,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.onEvent(MembershipDetailEvent.Refresh) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrim),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("Thử lại", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is MembershipDetailUiState.Success -> {
                    val membership = (uiState as MembershipDetailUiState.Success).activeMembership
                    MembershipSuccessContent(
                        membership = membership,
                        onNavigateToQrDisplay = onNavigateToQrDisplay,
                        onNavigateToPackages = onNavigateToPackages
                    )
                }
            }
        }
    }
}

@Composable
private fun MembershipSuccessContent(
    membership: ActiveMembership,
    onNavigateToQrDisplay: () -> Unit,
    onNavigateToPackages: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Premium ATM Card ────────────────────────────────────────
        ActiveMembershipCard(membership = membership)

        // ── Action Buttons ─────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // QR Button
            OutlinedButton(
                onClick = onNavigateToQrDisplay,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(18.dp), tint = OrangeLight)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hiện mã QR", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            }

            // Renew Button (gradient)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(listOf(OrangeLight, OrangePrim))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onNavigateToPackages,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gia hạn", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // ── Section Title ───────────────────────────────────────────
        Text(
            "Thông tin gói",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )

        // ── Info Card (Dark) ────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                @Composable
                fun InfoRow(label: String, value: String, valueColor: Color = TextPrimary) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = TextSecond, fontSize = 13.sp)
                        Text(
                            value,
                            color = valueColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                InfoRow("Gói tập", membership.planName)
                HorizontalDivider(color = BorderDark)
                InfoRow(
                    "Chi nhánh",
                    if (membership.planType == com.gymapp.android.domain.model.membership.PlanType.ALL)
                        "Tất cả chi nhánh" else membership.branchName ?: "–"
                )
                HorizontalDivider(color = BorderDark)
                InfoRow(
                    "Ngày bắt đầu",
                    membership.startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
                HorizontalDivider(color = BorderDark)
                InfoRow(
                    "Ngày hết hạn",
                    membership.endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    valueColor = if (membership.daysLeft <= 7) ErrorRed else TextPrimary
                )
                HorizontalDivider(color = BorderDark)
                InfoRow(
                    "Còn lại",
                    "${membership.daysLeft} ngày",
                    valueColor = when {
                        membership.daysLeft <= 7  -> ErrorRed
                        membership.daysLeft <= 30 -> OrangeLight
                        else                      -> SuccessGreen
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun EmptyMembershipState(onNavigateToPackages: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration circles
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A1A08))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(OrangeLight, OrangePrim))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Chưa có gói hội viên",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Mua một gói thành viên để bắt đầu\nhành trình của bạn tại GYM FITNESS!",
            color = TextSecond,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Brush.horizontalGradient(listOf(OrangeLight, OrangePrim))),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNavigateToPackages,
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Xem các gói hội viên",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
