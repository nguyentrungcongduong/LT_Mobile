package com.gymapp.modules.membership.listener;

import com.gymapp.modules.membership.event.MembershipExpiredEvent;
import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Listener xử lý MembershipExpiredEvent
 * 
 * - Tạo notification trong database
 * - Có thể mở rộng để gửi push notification qua FCM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleMembershipExpiredEvent(MembershipExpiredEvent event) {
        log.info("Handling MembershipExpiredEvent: {}", event);

        try {
            // Lấy user để đảm bảo tồn tại
            User user = userRepository.findById(event.getUserId()).orElse(null);
            if (user == null) {
                log.error("User not found for membership expired event: userId={}", event.getUserId());
                return;
            }

            // Tạo notification
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle("Gói hội viên đã hết hạn");
            notification.setBody(String.format(
                "Gói %s của bạn đã hết hạn vào ngày %s. Vui lòng gia hạn để tiếp tục tập luyện!",
                event.getPlanName(),
                event.getExpiredDate()
            ));
            notification.setType(com.gymapp.modules.notification.enums.NotificationType.MEMBERSHIP_EXPIRED);
            notification.setRefId(event.getMembershipId());
            notification.setIsRead(false);
            notification.setCreatedAt(OffsetDateTime.now());

            notificationRepository.save(notification);
            log.info("Created notification for expired membership: membershipId={}, userId={}", 
                    event.getMembershipId(), event.getUserId());

            // TODO: Gửi push notification qua Firebase Cloud Messaging (FCM)
            // if (user.getFcmToken() != null) {
            //     fcmService.sendNotification(user.getFcmToken(), notification.getTitle(), notification.getBody());
            // }

        } catch (Exception e) {
            log.error("Error handling MembershipExpiredEvent: {}", event, e);
        }
    }
}
