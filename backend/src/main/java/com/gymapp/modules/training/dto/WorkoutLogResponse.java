package com.gymapp.modules.training.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class WorkoutLogResponse {
    private UUID id;
    private UUID planId;
    private String planName;
    private LocalDate logDate;
    private Integer durationMinutes;
    private String notes;
    private boolean completed;
    private OffsetDateTime createdAt;
}
