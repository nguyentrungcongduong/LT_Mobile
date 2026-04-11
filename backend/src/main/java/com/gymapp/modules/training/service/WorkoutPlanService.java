package com.gymapp.modules.training.service;

import com.gymapp.modules.training.dto.WorkoutPlanRequest;
import com.gymapp.modules.training.dto.WorkoutPlanResponse;
import com.gymapp.modules.training.enums.WpType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WorkoutPlanService {
    Page<WorkoutPlanResponse> getWorkoutPlans(UUID currentUserId, WpType type, Pageable pageable);
    WorkoutPlanResponse createWorkoutPlan(UUID currentUserId, WorkoutPlanRequest request);
    WorkoutPlanResponse updateWorkoutPlan(UUID currentUserId, UUID planId, WorkoutPlanRequest request);
    WorkoutPlanResponse getWorkoutPlanById(UUID currentUserId, UUID planId);
}
