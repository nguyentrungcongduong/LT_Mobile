package com.gymapp.modules.checkin.entity;

import com.gymapp.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * CheckinLog — bản ghi lịch sử check-in qua QR (JWT + Redis one-time token).
 * Khác với Checkin (check-in tĩnh cũ), CheckinLog lưu thêm branchId và method.
 */
@Entity
@Table(name = "checkin_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "checkin_time", nullable = false)
    private OffsetDateTime checkinTime;

    /** ID của QR token đã dùng (jti claim), để audit */
    @Column(name = "qr_token_jti", length = 36)
    private String qrTokenJti;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
