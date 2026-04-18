package com.gymapp.modules.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service gửi FCM push notification qua Firebase Admin SDK.
 *
 * Nếu Firebase chưa được khởi tạo (thiếu service account file),
 * method sẽ log warning và bỏ qua — không throw exception để không block flow.
 */
@Slf4j
@Service
public class FcmService {

    /**
     * Gửi push notification tới một device token cụ thể.
     *
     * @param fcmToken   FCM device token của user
     * @param title      Tiêu đề notification
     * @param body       Nội dung notification
     * @param dataKey    Key của extra data (nullable)
     * @param dataValue  Value của extra data (nullable)
     */
    public void sendPush(String fcmToken, String title, String body,
                         String dataKey, String dataValue) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM token is null or blank, skip push notification");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized. Skipping push: title={}", title);
            return;
        }

        try {
            Message.Builder builder = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(fcmToken);

            if (dataKey != null && dataValue != null) {
                builder.putData(dataKey, dataValue);
            }

            String response = FirebaseMessaging.getInstance().send(builder.build());
            log.info("FCM push sent successfully. MessageId={}, title={}", response, title);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM push: errorCode={}, title={}, error={}",
                    e.getMessagingErrorCode(), title, e.getMessage());
        }
    }

    /**
     * Gửi push notification không kèm extra data.
     */
    public void sendPush(String fcmToken, String title, String body) {
        sendPush(fcmToken, title, body, null, null);
    }
}
