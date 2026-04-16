package com.gymapp.modules.training.scheduler;

import com.gymapp.modules.notification.entity.Notification;
import com.gymapp.modules.notification.enums.NotificationType;
import com.gymapp.modules.notification.repository.NotificationRepository;
import com.gymapp.modules.notification.service.FcmService;
import com.gymapp.modules.training.entity.WorkoutSchedule;
import com.gymapp.modules.training.repository.WorkoutScheduleRepository;
import com.gymapp.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Scheduler chạy mỗi ngày lúc 07:00 (server time).
 *
 * Scan bảng workout_schedules, tìm tất cả user có lịch tập
 * vào ngày hôm nay trong tuần, rồi gửi FCM push nhắc tập
 * và lưu notification log vào DB.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkoutReminderScheduler {

    private final WorkoutScheduleRepository workoutScheduleRepository;
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    /**
     * Map Java DayOfWeek → string 3 ký tự dùng trong DB
     */
    private static final Map<DayOfWeek, String> DAY_MAP = Map.of(
            DayOfWeek.MONDAY,    "MON",
            DayOfWeek.TUESDAY,   "TUE",
            DayOfWeek.WEDNESDAY, "WED",
            DayOfWeek.THURSDAY,  "THU",
            DayOfWeek.FRIDAY,    "FRI",
            DayOfWeek.SATURDAY,  "SAT",
            DayOfWeek.SUNDAY,    "SUN"
    );

    /**
     * Chạy lúc 07:00 sáng mỗi ngày (cron: 0 0 7 * * ?)
     */
    @Scheduled(cron = "0 0 7 * * ?")
    @Transactional
    public void sendDailyWorkoutReminders() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String dayCode = DAY_MAP.get(today);
        String dayDisplayVi = today.getDisplayName(TextStyle.FULL, new Locale("vi", "VN"));

        log.info("WorkoutReminderScheduler started for day={} ({})", dayCode, dayDisplayVi);

        List<WorkoutSchedule> schedules =
                workoutScheduleRepository.findByDayOfWeekWithUser(dayCode);

        if (schedules.isEmpty()) {
            log.info("No workout schedules found for {}", dayCode);
            return;
        }

        log.info("Found {} user(s) with workout schedule for {}", schedules.size(), dayCode);

        int successCount = 0;
        for (WorkoutSchedule ws : schedules) {
            try {
                User user = ws.getUser();
                String title = "Đến giờ tập luyện rồi! 💪";
                String body  = String.format(
                        "Hôm nay là %s — lịch tập của bạn vào lúc %s. Hãy bắt đầu để đạt mục tiêu!",
                        dayDisplayVi,
                        ws.getRemindTime().toString()
                );

                // Lưu notification log
                Notification notification = Notification.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .type(NotificationType.WORKOUT_REMINDER)
                        .isRead(false)
                        .sentAt(user.getFcmToken() != null ? OffsetDateTime.now() : null)
                        .build();
                notificationRepository.save(notification);

                // Gửi FCM push
                fcmService.sendPush(user.getFcmToken(), title, body,
                        "type", "WORKOUT_REMINDER");

                successCount++;
            } catch (Exception e) {
                log.error("Error sending workout reminder for userId={}: {}",
                        ws.getUser().getId(), e.getMessage());
            }
        }

        log.info("WorkoutReminderScheduler completed: sent {}/{} reminders for {}",
                successCount, schedules.size(), dayCode);
    }
}
