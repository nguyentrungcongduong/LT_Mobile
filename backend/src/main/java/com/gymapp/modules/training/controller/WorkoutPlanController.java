package com.gymapp.modules.training.controller;

import com.gymapp.common.security.UserDetailsImpl;
import com.gymapp.modules.training.dto.WorkoutPlanRequest;
import com.gymapp.modules.training.dto.WorkoutPlanResponse;
import com.gymapp.modules.training.enums.WpType;
import com.gymapp.modules.training.service.WorkoutPlanService;
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
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @GetMapping
    public ResponseEntity<Page<WorkoutPlanResponse>> getWorkoutPlans(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam WpType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutPlanService.getWorkoutPlans(userDetails.getId(), type, pageable));
    }

    @PostMapping
    public ResponseEntity<WorkoutPlanResponse> createWorkoutPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.createWorkoutPlan(userDetails.getId(), request));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> updateWorkoutPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID planId,
            @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(workoutPlanService.updateWorkoutPlan(userDetails.getId(), planId, request));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlanResponse> getWorkoutPlanById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID planId) {
        return ResponseEntity.ok(workoutPlanService.getWorkoutPlanById(userDetails.getId(), planId));
    }
}
