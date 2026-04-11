package com.gymapp.modules.training.controller;

import com.gymapp.common.security.UserDetailsImpl;
import com.gymapp.modules.training.dto.WorkoutPlanRequest;
import com.gymapp.modules.training.dto.WorkoutPlanResponse;
import com.gymapp.modules.training.enums.WpType;
import com.gymapp.modules.training.service.WorkoutPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workout-plans")
@RequiredArgsConstructor
@Tag(name = "Workout Plans", description = "CRUD API for Workout Plans (USER_CUSTOM and PT_ASSIGNED)")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @Operation(summary = "Get workout plans", description = "Get list of paginated workout plans for the current user by type")
    @GetMapping
    public ResponseEntity<Page<WorkoutPlanResponse>> getWorkoutPlans(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam WpType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutPlanService.getWorkoutPlans(userDetails.getId(), type, pageable));
    }

    @Operation(summary = "Get recommended workout plans", description = "Get list of recommended workout plans based on user's experience level")
    @GetMapping("/recommended")
    public ResponseEntity<Page<WorkoutPlanResponse>> getRecommendedPlans(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutPlanService.getRecommendedPlans(userDetails.getId(), pageable));
    }

    @Operation(summary = "Create workout plan", description = "Create a new workout plan with exercises list")
    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.createWorkoutPlan(userDetails.getId(), request));
    }

    @Operation(summary = "Update workout plan", description = "Update an existing workout plan and its exercises")
    @PutMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID planId,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(workoutPlanService.updateWorkoutPlan(userDetails.getId(), planId, request));
    }

    @Operation(summary = "Get workout plan details", description = "Get details of a specific workout plan by ID")
    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> getWorkoutPlanById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID planId) {
        return ResponseEntity.ok(workoutPlanService.getWorkoutPlanById(userDetails.getId(), planId));
    }
}
