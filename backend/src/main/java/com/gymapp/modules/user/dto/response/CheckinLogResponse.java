package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinLogResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private UUID branchId;

    /** Tên chi nhánh hoặc "Tất cả chi nhánh" nếu gói ALL */
    private String branchName;

    /** "ALL" hoặc "SINGLE" — để frontend hiển thị badge */
    private String planType;

    private LocalDate checkinDate;
    private OffsetDateTime checkinTime;
    private String qrTokenJti;
    private OffsetDateTime createdAt;
}
