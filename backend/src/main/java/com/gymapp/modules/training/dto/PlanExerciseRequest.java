package com.gymapp.modules.training.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class PlanExerciseRequest {
    private UUID exerciseId;
    private Integer sets;
    private Integer reps;
    private Integer restSeconds;
    private Integer orderIndex;
    private String notes;
}
