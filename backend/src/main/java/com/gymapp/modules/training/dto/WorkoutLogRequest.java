package com.gymapp.modules.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class WorkoutLogRequest {
    private UUID planId;
    
    @NotNull(message = "Log date is required")
    private LocalDate logDate;
    
    private Integer durationMinutes;
    private String notes;
    private Boolean completed;
}
