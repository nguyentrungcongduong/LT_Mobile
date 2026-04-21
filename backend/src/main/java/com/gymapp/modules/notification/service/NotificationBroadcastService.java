package com.gymapp.modules.notification.service;

import com.gymapp.modules.notification.dto.BroadcastNotificationRequest;
import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.enums.NotificationType;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationBroadcastService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    public void broadcast(BroadcastNotificationRequest request) {

        List<User> users = resolveUsers(request);

        List<Notification> notifications = new ArrayList<>();

        for (User user : users) {

            Notification notification = Notification.builder()
                    .user(user)
                    .title(request.getTitle())
                    .body(request.getBody())
                    .type(NotificationType.SYSTEM)
                    .isRead(false)
                    .sentAt(OffsetDateTime.now())
                    .build();

            notifications.add(notification);

            // gửi FCM nếu có token
            if (user.getFcmToken() != null) {
                fcmService.sendPush(
                        user.getFcmToken(),
                        request.getTitle(),
                        request.getBody()
                );
            }
        }

        notificationRepository.saveAll(notifications);
    }

    private List<User> resolveUsers(BroadcastNotificationRequest request) {

        return switch (request.getTargetGroup()) {
            case "ALL" -> userRepository.findAll();

            case "ACTIVE" -> userRepository.findByIsActiveTrue();

            case "PT" -> userRepository.findByRole(UserRole.PT);

            case "USER_IDS" -> userRepository.findByIdIn(request.getUserIds());

            default -> throw new RuntimeException("Invalid target group");
        };
    }
}
