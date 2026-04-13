package com.gymapp.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinLogResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private UUID branchId;
    private LocalDate checkinDate;
    private OffsetDateTime checkinTime;
    private String qrTokenJti;
    private OffsetDateTime createdAt;
}
