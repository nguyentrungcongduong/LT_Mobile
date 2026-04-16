package com.gymapp.modules.booking.listener;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.event.BookingCancelledEvent;
import com.gymapp.modules.booking.event.BookingConfirmedEvent;
import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.enums.NotificationType;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.notification.service.FcmService;
import com.gymapp.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Listener xử lý Booking events — gửi push notification + lưu log (Async).
 *
 * - BookingConfirmedEvent → push cho User + PT
 * - BookingCancelledEvent → push cho bên còn lại
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingNotificationListener {

    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKING CONFIRMED
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        Booking booking = event.getBooking();
        log.info("Handling BookingConfirmedEvent: bookingId={}", booking.getId());

        try {
            User user = booking.getUser();
            User pt   = booking.getPt();
            String scheduledStr = booking.getScheduledAt().format(DISPLAY_FMT);

            // --- push cho User ---
            String userTitle = "Đặt lịch thành công! 🎉";
            String userBody  = String.format(
                    "Buổi tập với PT %s đã được xác nhận vào lúc %s. Hẹn gặp bạn!",
                    pt.getFullName(), scheduledStr);

            saveNotification(user, userTitle, userBody,
                    NotificationType.BOOKING_CONFIRMED, booking.getId());
            fcmService.sendPush(user.getFcmToken(), userTitle, userBody,
                    "bookingId", booking.getId().toString());

            // --- push cho PT ---
            String ptTitle = "Bạn có lịch hẹn mới 📅";
            String ptBody  = String.format(
                    "Học viên %s đã đặt lịch buổi tập vào lúc %s.",
                    user.getFullName(), scheduledStr);

            saveNotification(pt, ptTitle, ptBody,
                    NotificationType.BOOKING_CONFIRMED, booking.getId());
            fcmService.sendPush(pt.getFcmToken(), ptTitle, ptBody,
                    "bookingId", booking.getId().toString());

        } catch (Exception e) {
            log.error("Error handling BookingConfirmedEvent: bookingId={}", booking.getId(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BOOKING CANCELLED
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void handleBookingCancelled(BookingCancelledEvent event) {
        Booking booking = event.getBooking();
        log.info("Handling BookingCancelledEvent: bookingId={}", booking.getId());

        try {
            User user = booking.getUser();
            User pt   = booking.getPt();
            String scheduledStr = booking.getScheduledAt().format(DISPLAY_FMT);

            // Xác định ai hủy
            boolean cancelledByUser = switch (booking.getCancelBy()) {
                case USER   -> true;
                case PT     -> false;
                case SYSTEM -> true; // Hệ thống hủy → thông báo cho user
            };

            if (cancelledByUser) {
                // Báo cho PT rằng user đã hủy
                String ptTitle = "Lịch hẹn bị hủy ❌";
                String ptBody  = String.format(
                        "Học viên %s đã hủy buổi tập vào lúc %s.",
                        user.getFullName(), scheduledStr);
                saveNotification(pt, ptTitle, ptBody,
                        NotificationType.BOOKING_CANCELLED, booking.getId());
                fcmService.sendPush(pt.getFcmToken(), ptTitle, ptBody,
                        "bookingId", booking.getId().toString());
            } else {
                // Báo cho User rằng PT đã hủy
                String userTitle = "Lịch hẹn bị hủy ❌";
                String userBody  = String.format(
                        "PT %s đã hủy buổi tập vào lúc %s. Bạn sẽ được hoàn tiền sớm.",
                        pt.getFullName(), scheduledStr);
                saveNotification(user, userTitle, userBody,
                        NotificationType.BOOKING_CANCELLED, booking.getId());
                fcmService.sendPush(user.getFcmToken(), userTitle, userBody,
                        "bookingId", booking.getId().toString());
            }

        } catch (Exception e) {
            log.error("Error handling BookingCancelledEvent: bookingId={}", booking.getId(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private void saveNotification(User recipient, String title, String body,
                                  NotificationType type, java.util.UUID refId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .title(title)
                .body(body)
                .type(type)
                .refId(refId)
                .isRead(false)
                .sentAt(recipient.getFcmToken() != null ? OffsetDateTime.now() : null)
                .build();
        notificationRepository.save(notification);
    }
}
