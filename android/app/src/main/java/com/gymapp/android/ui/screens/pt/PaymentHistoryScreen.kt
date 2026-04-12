package com.gymapp.android.ui.screens.pt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// ── Design Tokens ──────────────────────────────────────────────────────────────
private val BgPrimary      = Color(0xFFFFFFFF)
private val BgSecondary    = Color(0xFFF5F5F5)
private val BorderTertiary = Color(0xFFEBEBEB)
private val Tprimary       = Color(0xFF1A1A1A)
private val Tsecondary     = Color(0xFF6B6B6B)
private val BlueActive     = Color(0xFF185FA5)

// Status badge
private val SuccessBg = Color(0xFFEAF3DE); private val SuccessText = Color(0xFF3B6D11); private val SuccessBorder = Color(0xFFC0DD97)
private val FailedBg  = Color(0xFFFCEBEB); private val FailedText  = Color(0xFFA32D2D); private val FailedBorder  = Color(0xFFF7C1C1)
private val PendingBg = Color(0xFFFAEEDA); private val PendingText = Color(0xFF854F0B); private val PendingBorder = Color(0xFFFAC775)
private val RefundBg  = Color(0xFFE6F1FB); private val RefundText  = Color(0xFF185FA5); private val RefundBorder  = Color(0xFFB5D4F4)

// Icon backgrounds (unused, kept for future use)
private val IconBlueBg = Color(0xFFDCEEFB)
private val IconGreenBg = Color(0xFFD8F0E4)
private val IconTealBg  = Color(0xFFD0EEE9)

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
                title = { Text("Lịch sử giao dịch", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgSecondary),
                modifier = Modifier.border(0.5.dp, BorderTertiary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
        ) {
            // ── Filter Tabs ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = BorderTertiary)
            ) {
                tabs.forEachIndexed { index, label ->
                    val isActive = selectedTab == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectTab(index) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) BlueActive else Tsecondary
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(2.dp)
                                    .background(BlueActive)
                            )
                        }
                    }
                }
            }

            // ── Content ─────────────────────────────────────────────────────────
            when (val state = uiState) {
                is PaymentHistoryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlueActive)
                    }
                }
                is PaymentHistoryUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(state.message, color = Tsecondary, fontSize = 14.sp)
                            TextButton(onClick = { viewModel.loadHistory() }) {
                                Text("Thử lại", color = BlueActive)
                            }
                        }
                    }
                }
                is PaymentHistoryUiState.Success -> {
                    if (state.items.isEmpty() && !state.isLoadingMore) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Không có giao dịch nào", color = Tsecondary, fontSize = 14.sp)
                        }
                    } else {
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()

                        // Trigger load more when near the end
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
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Group by month
                            val grouped = state.items.groupBy { item ->
                                parseMonthLabel(item.createdAt)
                            }
                            grouped.forEach { (monthLabel, txList) ->
                                item(key = "header_$monthLabel") {
                                    Text(
                                        text = monthLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Tsecondary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                items(txList, key = { it.paymentId }) { txItem ->
                                    PaymentHistoryCard(item = txItem, currencyFormat = currencyFormat)
                                }
                            }

                            // Loading more footer
                            if (state.isLoadingMore) {
                                item(key = "loading_footer") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = BlueActive,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }

                            // End of list hint
                            if (!state.hasMore && state.items.isNotEmpty()) {
                                item(key = "end_hint") {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Đã hiển thị tất cả giao dịch", color = Tsecondary, fontSize = 12.sp)
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
        "SUCCESS"  -> listOf(SuccessBg, SuccessText, SuccessBorder, "Thành công")
        "FAILED"   -> listOf(FailedBg, FailedText, FailedBorder, "Thất bại")
        "REFUNDED" -> listOf(RefundBg, RefundText, RefundBorder, "Đã hoàn")
        else       -> listOf(PendingBg, PendingText, PendingBorder, "Đang xử lý")
    }

    val dateStr = formatDate(item.createdAt)
    val amountPrefix = when (item.status) {
        "REFUNDED" -> "+"
        else       -> "−"
    }
    val amountColor = when (item.status) {
        "REFUNDED" -> Color(0xFF1D9E75)
        "FAILED", "PENDING" -> Tsecondary
        else -> Tprimary
    }
    val providerLabel = when (item.provider) {
        "VNPAY" -> "VNPay"
        "MOMO"  -> "MoMo"
        else    -> item.provider
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, BorderTertiary, RoundedCornerShape(10.dp))
            .background(BgPrimary)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Center
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.transactionName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Tprimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dateStr · $providerLabel",
                fontSize = 13.sp,
                color = Tsecondary
            )
            // Status badge
            Box(
                modifier = Modifier
                    .border(0.5.dp, statusBorder as Color, RoundedCornerShape(6.dp))
                    .background(statusBg as Color, RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(statusLabel as String, fontSize = 11.sp, color = statusText as Color, fontWeight = FontWeight.Medium)
            }
        }

        // Amount
        Text(
            text = "$amountPrefix${currencyFormat.format(item.amount)}",
            fontSize = 15.sp,
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
