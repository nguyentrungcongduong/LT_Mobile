package com.gymapp.modules.training.dto;

import com.gymapp.modules.training.enums.WpType;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WorkoutPlanResponse {
    private UUID id;
    private String name;
    private String description;
    private WpType planType;
    private String assignedToName;
    private String createdByName;
    private List<PlanExerciseResponse> exercises;
}
