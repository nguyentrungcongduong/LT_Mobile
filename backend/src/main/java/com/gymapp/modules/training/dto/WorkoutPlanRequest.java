package com.gymapp.modules.training.dto;

import com.gymapp.modules.training.enums.WpType;
import com.gymapp.modules.training.enums.TargetLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @NotNull(message = "Plan type is required")
    private WpType planType;
    private TargetLevel targetLevel;
    private UUID assignedTo;
    private List<PlanExerciseRequest> exercises;
    private String scheduledDate;
}
