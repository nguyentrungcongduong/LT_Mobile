package com.gymapp.modules.training.entity;

import com.gymapp.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Lịch tập cá nhân của user.
 * Mỗi record = 1 ngày trong tuần + giờ nhắc nhở.
 *
 * Scheduler hàng ngày lúc 7:00 sáng sẽ scan bảng này
 * và gửi FCM push nhắc các user có lịch tập ngày hôm đó.
 */
@Entity
@Table(name = "workout_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Ngày trong tuần: MON, TUE, WED, THU, FRI, SAT, SUN
     */
    @Column(name = "day_of_week", nullable = false, length = 3)
    private String dayOfWeek;

    /**
     * Giờ nhắc (local time). Mặc định 06:00.
     * Scheduler chạy lúc 07:00 sẽ đọc field này để cá nhân hoá.
     */
    @Column(name = "remind_time", nullable = false)
    @Builder.Default
    private LocalTime remindTime = LocalTime.of(6, 0);

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
