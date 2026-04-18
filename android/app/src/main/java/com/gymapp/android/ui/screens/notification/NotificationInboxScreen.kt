package com.gymapp.android.ui.screens.notification

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymapp.android.data.remote.api.NotificationDto

// ── Design tokens (consistent với ProfileScreen) ─────────────────────────────
private val BgColor       = Color(0xFFF8F9FA)
private val CardBg        = Color.White
private val OrangePrimary = Color(0xFFFF5722)
private val TextDark      = Color(0xFF111827)
private val TextGray      = Color(0xFF6B7280)
private val UnreadBg      = Color(0xFFFFF3F0)   // Nền cam nhạt cho chưa đọc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thông báo", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        if (uiState is NotificationUiState.Success) {
                            val count = (uiState as NotificationUiState.Success).unreadCount
                            if (count > 0)
                                Text("$count chưa đọc", fontSize = 12.sp, color = OrangePrimary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    if (uiState is NotificationUiState.Success &&
                        (uiState as NotificationUiState.Success).unreadCount > 0
                    ) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Đọc hết", color = OrangePrimary, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor)
            )
        },
        containerColor = BgColor
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is NotificationUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OrangePrimary
                    )
                }
                is NotificationUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null,
                            tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(state.message, color = TextGray, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.load() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) { Text("Thử lại") }
                    }
                }
                is NotificationUiState.Success -> {
                    if (state.notifications.isEmpty()) {
                        EmptyNotifications(modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.notifications, key = { it.id }) { notif ->
                                NotificationItem(
                                    notification = notif,
                                    onClick = {
                                        if (!notif.isRead) viewModel.markAsRead(notif.id)
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

// ── Notification Item ─────────────────────────────────────────────────────────

@Composable
private fun NotificationItem(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (notification.isRead) CardBg else UnreadBg,
        animationSpec = tween(300),
        label = "notif_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon tròn
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBgColor(notification.type), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = notifIcon(notification.type),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )
                // Unread dot
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = notification.body,
                fontSize = 13.sp,
                color = TextGray,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatRelativeTime(notification.createdAt),
                fontSize = 11.sp,
                color = TextGray.copy(alpha = 0.7f)
            )
        }
    }

    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp,
        modifier = Modifier.padding(start = 72.dp))
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyNotifications(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFFFF3F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.NotificationsNone, contentDescription = null,
                tint = OrangePrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Chưa có thông báo nào", fontWeight = FontWeight.Bold,
            fontSize = 16.sp, color = TextDark)
        Spacer(Modifier.height(8.dp))
        Text("Các thông báo về lịch tập, booking\nvà hội viên sẽ xuất hiện tại đây.",
            fontSize = 13.sp, color = TextGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun notifIcon(type: String): ImageVector = when {
    type.contains("BOOKING")    -> Icons.Default.CalendarToday
    type.contains("WORKOUT")    -> Icons.Default.FitnessCenter
    type.contains("MEMBERSHIP") -> Icons.Default.CardMembership
    type.contains("PAYMENT")    -> Icons.Default.Payment
    else                        -> Icons.Default.Notifications
}

private fun iconBgColor(type: String): Color = when {
    type.contains("BOOKING")    -> Color(0xFF3F51B5)
    type.contains("WORKOUT")    -> Color(0xFFFF5722)
    type.contains("MEMBERSHIP") -> Color(0xFF4CAF50)
    type.contains("PAYMENT")    -> Color(0xFF9C27B0)
    else                        -> Color(0xFF607D8B)
}

private fun formatRelativeTime(isoString: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault())
        val date = sdf.parse(isoString) ?: return isoString
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000            -> "Vừa xong"
            diff < 3_600_000         -> "${diff / 60_000} phút trước"
            diff < 86_400_000        -> "${diff / 3_600_000} giờ trước"
            diff < 2 * 86_400_000    -> "Hôm qua"
            else                     -> {
                val fmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                fmt.format(date)
            }
        }
    } catch (e: Exception) { isoString }
}
