package com.gymapp.modules.training.controller;

import com.gymapp.common.security.UserDetailsImpl;
import com.gymapp.modules.training.dto.WorkoutLogRequest;
import com.gymapp.modules.training.dto.WorkoutLogResponse;
import com.gymapp.modules.training.service.WorkoutLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/workout-logs")
@RequiredArgsConstructor
@Tag(name = "Workout Logs", description = "API for Workout Logs")
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;

    @Operation(summary = "Get workout logs", description = "Get list of paginated workout logs by date range")
    @GetMapping
    public ResponseEntity<Page<WorkoutLogResponse>> getWorkoutLogs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workoutLogService.getWorkoutLogs(userDetails.getId(), from, to, pageable));
    }

    @Operation(summary = "Create workout log", description = "Log a new workout session")
    @PostMapping
    public ResponseEntity<WorkoutLogResponse> createWorkoutLog(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody WorkoutLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutLogService.createWorkoutLog(userDetails.getId(), request));
    }
}
