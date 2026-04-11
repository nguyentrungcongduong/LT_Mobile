package com.gymapp.modules.training.service;

import com.gymapp.modules.training.dto.WorkoutLogRequest;
import com.gymapp.modules.training.dto.WorkoutLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface WorkoutLogService {
    WorkoutLogResponse createWorkoutLog(UUID currentUserId, WorkoutLogRequest request);
    Page<WorkoutLogResponse> getWorkoutLogs(UUID currentUserId, LocalDate from, LocalDate to, Pageable pageable);
}
