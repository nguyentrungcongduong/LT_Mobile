package com.gymapp.modules.notification.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API quản lý notification inbox của user.
 *
 * 
 * GET /api/v1/notifications — lấy danh sách
 * PUT /api/v1/notifications/read-all — đánh dấu tất cả đã đọc
 * PUT /api/v1/notifications/{id}/read — đánh dấu 1 đã đọc
 * 
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<NotificationDto>> getNotifications() {
        User user = getCurrentUser();
        List<Notification> list = notificationRepository.findByUserId(user.getId());
        List<NotificationDto> dtos = list.stream()
                .map(n -> new NotificationDto(
                        n.getId().toString(),
                        n.getTitle(),
                        n.getBody(),
                        n.getType().name(),
                        n.getRefId() != null ? n.getRefId().toString() : null,
                        n.getIsRead(),
                        n.getSentAt() != null ? n.getSentAt().toString() : null,
                        n.getCreatedAt().toString()))
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos, "Lấy thông báo thành công");
    }

    @Transactional
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        User user = getCurrentUser();
        notificationRepository.markAllAsRead(user.getId());
        return ApiResponse.ok(null, "Đã đánh dấu tất cả là đã đọc");
    }

    @Transactional
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id) {
        User user = getCurrentUser();
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
        return ApiResponse.ok(null, "Đã đánh dấu đã đọc");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── inner DTO ─────────────────────────────────────────────────────────────
    record NotificationDto(

            String id,
            String title,
            String body,
            String type,
            String refId,
            Boolean isRead,
            String sentAt,
            String createdAt) {
    }

}
