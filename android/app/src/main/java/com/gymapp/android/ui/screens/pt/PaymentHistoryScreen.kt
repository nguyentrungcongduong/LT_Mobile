package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.PaymentHistoryDto
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Dark Design Tokens ─────────────────────────────────────────────────────────
private val PHBgPrimary    = Color(0xFF0F0F12)
private val PHBgCard       = Color(0xFF1C1C1E)
private val PHBgElevated   = Color(0xFF242428)
private val PHBorder       = Color(0xFF2A2A2E)
private val PHTextPrimary  = Color(0xFFF2F2F7)
private val PHTextSecondary= Color(0xFF8E8E93)
private val PHOrange       = Color(0xFFFF6B2B)
private val PHOrangeGlow   = Color(0xFFFF8C00)

// Status badge colors (dark-mode friendly)
private val PHSuccessBg     = Color(0xFF1A2E14); private val PHSuccessText = Color(0xFF6FCF44); private val PHSuccessBorder = Color(0xFF2E5022)
private val PHFailedBg      = Color(0xFF2E1414); private val PHFailedText  = Color(0xFFEF5350); private val PHFailedBorder  = Color(0xFF522222)
private val PHPendingBg     = Color(0xFF2A1E08); private val PHPendingText = Color(0xFFF5A623); private val PHPendingBorder = Color(0xFF4A3410)
private val PHRefundBg      = Color(0xFF0D1F2E); private val PHRefundText  = Color(0xFF4FC3F7); private val PHRefundBorder  = Color(0xFF1A3D5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tabs = listOf("Tất cả", "Lịch PT", "Hội viên", "Hoàn tiền")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch sử giao dịch",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PHTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PHBgElevated, CircleShape)
                                .border(1.dp, PHBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Trở về",
                                tint = PHTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PHBgCard,
                    titleContentColor = PHTextPrimary
                )
            )
        },
        containerColor = PHBgPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Filter Tabs ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PHBgCard)
                    .border(width = 0.5.dp, color = PHBorder)
            ) {
                tabs.forEachIndexed { index, label ->
                    val isActive = selectedTab == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectTab(index) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) PHOrange else PHTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(2.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(PHOrange, PHOrangeGlow))
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            // ── Content ──────────────────────────────────────────────────────────
            when (val state = uiState) {
                is PaymentHistoryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PHOrange)
                    }
                }
                is PaymentHistoryUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.message, color = PHTextSecondary, fontSize = 14.sp)
                            Button(
                                onClick = { viewModel.loadHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = PHOrange)
                            ) {
                                Text("Thử lại", color = Color.White)
                            }
                        }
                    }
                }
                is PaymentHistoryUiState.Success -> {
                    if (state.items.isEmpty() && !state.isLoadingMore) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("💳", fontSize = 40.sp)
                                Text("Chưa có giao dịch nào", color = PHTextSecondary, fontSize = 14.sp)
                            }
                        }
                    } else {
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        val shouldLoadMore = remember {
                            derivedStateOf {
                                val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                                val totalItems = listState.layoutInfo.totalItemsCount
                                lastVisibleIndex >= totalItems - 3
                            }
                        }
                        LaunchedEffect(shouldLoadMore.value) {
                            if (shouldLoadMore.value && state.hasMore) {
                                viewModel.loadNextPage()
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val grouped = state.items.groupBy { item -> parseMonthLabel(item.createdAt) }
                            grouped.forEach { (monthLabel, txList) ->
                                item(key = "header_$monthLabel") {
                                    Text(
                                        text = monthLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PHTextSecondary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                                    )
                                }
                                items(txList, key = { it.paymentId }) { txItem ->
                                    PaymentHistoryCard(item = txItem, currencyFormat = currencyFormat)
                                }
                            }

                            if (state.isLoadingMore) {
                                item(key = "loading_footer") {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PHOrange, strokeWidth = 2.dp)
                                    }
                                }
                            }

                            if (!state.hasMore && state.items.isNotEmpty()) {
                                item(key = "end_hint") {
                                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                                        Text("• Đã hiển thị tất cả •", color = PHTextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryCard(item: PaymentHistoryDto, currencyFormat: NumberFormat) {
    val (statusBg, statusText, statusBorder, statusLabel) = when (item.status) {
        "SUCCESS"  -> listOf(PHSuccessBg, PHSuccessText, PHSuccessBorder, "Thành công")
        "FAILED"   -> listOf(PHFailedBg, PHFailedText, PHFailedBorder, "Thất bại")
        "REFUNDED" -> listOf(PHRefundBg, PHRefundText, PHRefundBorder, "Đã hoàn")
        else       -> listOf(PHPendingBg, PHPendingText, PHPendingBorder, "Đang xử lý")
    }

    val dateStr = formatDate(item.createdAt)
    val amountPrefix = if (item.status == "REFUNDED") "+" else "−"
    val amountColor = when (item.status) {
        "REFUNDED" -> Color(0xFF4FCF97)
        "FAILED"   -> PHTextSecondary
        else       -> PHTextPrimary
    }
    val providerLabel = when (item.provider) {
        "VNPAY" -> "VNPay"
        "MOMO"  -> "MoMo"
        else    -> item.provider
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, PHBorder, RoundedCornerShape(14.dp))
            .background(PHBgCard)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.transactionName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PHTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dateStr · $providerLabel",
                fontSize = 12.sp,
                color = PHTextSecondary
            )
            // Status badge
            Box(
                modifier = Modifier
                    .border(0.5.dp, statusBorder as Color, RoundedCornerShape(6.dp))
                    .background(statusBg as Color, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(statusLabel as String, fontSize = 11.sp, color = statusText as Color, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Amount — right aligned
        Text(
            text = "$amountPrefix${currencyFormat.format(item.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

private fun parseMonthLabel(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "Không rõ"
    return try {
        val dt = OffsetDateTime.parse(createdAt)
        "Tháng ${dt.monthValue}, ${dt.year}"
    } catch (e: Exception) { "Không rõ" }
}

private fun formatDate(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "--"
    return try {
        val dt = OffsetDateTime.parse(createdAt)
        dt.format(DateTimeFormatter.ofPattern("dd/MM"))
    } catch (e: Exception) { "--" }
}
