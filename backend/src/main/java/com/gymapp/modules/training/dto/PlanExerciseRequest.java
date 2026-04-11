package com.gymapp.modules.training.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PlanExerciseRequest {
    private UUID exerciseId;
    private Integer sets;
    private Integer reps;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;
}
