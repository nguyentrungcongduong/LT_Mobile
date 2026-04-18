package com.gymapp.modules.membership.listener;

import com.gymapp.modules.membership.event.MembershipExpiredEvent;
import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.enums.NotificationType;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.notification.service.FcmService;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Listener xử lý MembershipExpiredEvent (Async, không block booking flow)
 *
 * - Tạo notification log trong database
 * - Gửi FCM push notification nhắc user gia hạn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Async
    @EventListener
    public void handleMembershipExpiredEvent(MembershipExpiredEvent event) {
        log.info("Handling MembershipExpiredEvent: {}", event);

        try {
            User user = userRepository.findById(event.getUserId()).orElse(null);
            if (user == null) {
                log.error("User not found for membership expired event: userId={}", event.getUserId());
                return;
            }

            String title = "Gói hội viên đã hết hạn";
            String body = String.format(
                    "Gói %s của bạn đã hết hạn vào ngày %s. Vui lòng gia hạn để tiếp tục tập luyện!",
                    event.getPlanName(),
                    event.getExpiredDate()
            );

            // 1. Lưu notification log vào DB
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .body(body)
                    .type(NotificationType.MEMBERSHIP_EXPIRED)
                    .refId(event.getMembershipId())
                    .isRead(false)
                    .sentAt(user.getFcmToken() != null ? OffsetDateTime.now() : null)
                    .build();

            notificationRepository.save(notification);
            log.info("Saved notification log for expired membership: membershipId={}, userId={}",
                    event.getMembershipId(), event.getUserId());

            // 2. Gửi FCM push notification
            fcmService.sendPush(user.getFcmToken(), title, body,
                    "membershipId", event.getMembershipId().toString());

        } catch (Exception e) {
            log.error("Error handling MembershipExpiredEvent: {}", event, e);
        }
    }
}
