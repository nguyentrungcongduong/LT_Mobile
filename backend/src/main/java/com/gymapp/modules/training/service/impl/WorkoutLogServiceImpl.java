package com.gymapp.modules.training.service.impl;

import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.training.dto.WorkoutLogRequest;
import com.gymapp.modules.training.dto.WorkoutLogResponse;
import com.gymapp.modules.training.entity.WorkoutLog;
import com.gymapp.modules.training.entity.WorkoutPlan;
import com.gymapp.modules.training.repository.WorkoutLogRepository;
import com.gymapp.modules.training.repository.WorkoutPlanRepository;
import com.gymapp.modules.training.service.WorkoutLogService;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutLogServiceImpl implements WorkoutLogService {

    private final WorkoutLogRepository workoutLogRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;

    @Override
    @Transactional
    public WorkoutLogResponse createWorkoutLog(UUID currentUserId, WorkoutLogRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        WorkoutPlan plan = null;
        if (request.getPlanId() != null) {
            plan = workoutPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Workout plan not found"));
        }

        WorkoutLog log = WorkoutLog.builder()
                .user(user)
                .workoutPlan(plan)
                .logDate(request.getLogDate())
                .durationMinutes(request.getDurationMinutes())
                .notes(request.getNotes())
                .completed(request.getCompleted() != null ? request.getCompleted() : false)
                .build();

        WorkoutLog savedLog = workoutLogRepository.save(log);
        return mapToResponse(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutLogResponse> getWorkoutLogs(UUID currentUserId, LocalDate from, LocalDate to, Pageable pageable) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();

        Page<WorkoutLog> logs = workoutLogRepository.findByUserIdAndDateRange(currentUserId, from, to, pageable);
        return logs.map(this::mapToResponse);
    }

    private WorkoutLogResponse mapToResponse(WorkoutLog log) {
        return WorkoutLogResponse.builder()
                .id(log.getId())
                .planId(log.getWorkoutPlan() != null ? log.getWorkoutPlan().getId() : null)
                .planName(log.getWorkoutPlan() != null ? log.getWorkoutPlan().getName() : null)
                .logDate(log.getLogDate())
                .durationMinutes(log.getDurationMinutes())
                .notes(log.getNotes())
                .completed(log.isCompleted())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
