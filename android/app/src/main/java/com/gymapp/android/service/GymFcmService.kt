package com.gymapp.android.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gymapp.android.MainActivity
import com.gymapp.android.R
import com.gymapp.android.data.local.TokenStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 *
 * - Nhận push notification khi app đang mở (foreground)
 * - Hiển thị system notification (Android Notification Drawer)
 * - Tự động đăng ký token mới lên backend khi refresh
 */
@AndroidEntryPoint
class GymFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    @Inject
    lateinit var fcmTokenUploader: FcmTokenUploader

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID_GENERAL   = "gym_general"
        const val CHANNEL_ID_BOOKING   = "gym_booking"
        const val CHANNEL_ID_WORKOUT   = "gym_workout"
        const val CHANNEL_ID_MEMBERSHIP = "gym_membership"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                listOf(
                    NotificationChannel(CHANNEL_ID_GENERAL, "Thông báo chung", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_ID_BOOKING, "Lịch hẹn PT", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Xác nhận, hủy lịch, nhắc lịch buổi PT"
                    },
                    NotificationChannel(CHANNEL_ID_WORKOUT, "Nhắc tập luyện", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Nhắc lịch tập hàng ngày"
                    },
                    NotificationChannel(CHANNEL_ID_MEMBERSHIP, "Hội viên", NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Hết hạn, gia hạn membership"
                    }
                ).forEach { manager?.createNotificationChannel(it) }
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Nhận message khi app foreground — phải hiển thị notification thủ công
    // ──────────────────────────────────────────────────────────────────────────
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "GymApp"
        val body  = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val type  = remoteMessage.data["type"] ?: ""

        showSystemNotification(title, body, type)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Token refresh — tự động upload lên backend
    // ──────────────────────────────────────────────────────────────────────────
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Chỉ upload nếu user đã login
        if (tokenStorage.getAccessToken() != null) {
            serviceScope.launch {
                fcmTokenUploader.upload(token)
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Show notification helper
    // ──────────────────────────────────────────────────────────────────────────
    private fun showSystemNotification(title: String, body: String, type: String) {
        val channelId = when {
            type.contains("BOOKING")    -> CHANNEL_ID_BOOKING
            type.contains("WORKOUT")    -> CHANNEL_ID_WORKOUT
            type.contains("MEMBERSHIP") -> CHANNEL_ID_MEMBERSHIP
            else                        -> CHANNEL_ID_GENERAL
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
