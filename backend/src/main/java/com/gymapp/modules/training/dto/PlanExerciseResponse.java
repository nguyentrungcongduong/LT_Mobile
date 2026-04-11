package com.gymapp.modules.training.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlanExerciseResponse {
    private Integer orderIndex;
    private String exerciseName;
    private String muscleGroup;
    private Integer sets;
    private Integer reps;
    private Integer restSeconds;
    private String notes;
}
