package com.gymapp.modules.training.service.impl;

import com.gymapp.common.exception.ForbiddenException;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.training.dto.PlanExerciseRequest;
import com.gymapp.modules.training.dto.PlanExerciseResponse;
import com.gymapp.modules.training.dto.WorkoutPlanRequest;
import com.gymapp.modules.training.dto.WorkoutPlanResponse;
import com.gymapp.modules.training.entity.Exercise;
import com.gymapp.modules.training.entity.PlanExercise;
import com.gymapp.modules.training.entity.WorkoutPlan;
import com.gymapp.modules.training.enums.WpType;
import com.gymapp.modules.training.enums.TargetLevel;
import com.gymapp.modules.training.repository.ExerciseRepository;
import com.gymapp.modules.training.repository.WorkoutPlanRepository;
import com.gymapp.modules.training.service.WorkoutPlanService;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutPlanServiceImpl implements WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutPlanResponse> getWorkoutPlans(UUID currentUserId, WpType type, Pageable pageable) {
        Page<WorkoutPlan> plans = workoutPlanRepository.findAllByTypeAndUser(type, currentUserId, pageable);
        return plans.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkoutPlanResponse> getRecommendedPlans(UUID currentUserId, Pageable pageable) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        TargetLevel targetLevel = TargetLevel.BEGINNER;
        if (user.getExperienceLevel() != null) {
            targetLevel = TargetLevel.valueOf(user.getExperienceLevel().name());
        }

        Page<WorkoutPlan> plans = workoutPlanRepository.findRecommendedPlans(targetLevel, WpType.USER_CUSTOM, pageable);
        return plans.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse createWorkoutPlan(UUID currentUserId, WorkoutPlanRequest request) {
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        if (request.getPlanType() == WpType.PT_ASSIGNED && creator.getRole() != UserRole.PT) {
            throw new ForbiddenException("Only PTs can create assigned plans.");
        }

        User assignedTo = null;
        if (request.getPlanType() == WpType.PT_ASSIGNED) {
            if (request.getAssignedTo() == null) {
                throw new IllegalArgumentException("Assigned user is required for PT_ASSIGNED plan.");
            }
            assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Assigned user not found"));
        }

        WorkoutPlan plan = WorkoutPlan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(creator)
                .planType(request.getPlanType())
                .targetLevel(request.getTargetLevel())
                .assignedTo(assignedTo)
                .isActive(true)
                .exercises(new ArrayList<>())
                .scheduledDate(request.getScheduledDate() != null
                        ? LocalDate.parse(request.getScheduledDate())
                        : null)
                .build();

        List<PlanExercise> exercises = mapRequestExercises(request.getExercises(), plan);
        plan.getExercises().addAll(exercises);

        WorkoutPlan savedPlan = workoutPlanRepository.save(plan);
        return mapToResponse(savedPlan);
    }

    @Override
    @Transactional
    public WorkoutPlanResponse updateWorkoutPlan(UUID currentUserId, UUID planId, WorkoutPlanRequest request) {
        WorkoutPlan plan = workoutPlanRepository.findByIdWithExercises(planId)
                .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Workout plan not found"));

        if (!plan.getCreatedBy().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the creator can update the plan");
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setTargetLevel(request.getTargetLevel());

        // Update assigned user if changed
        if (request.getPlanType() == WpType.PT_ASSIGNED && request.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Assigned user not found"));
            plan.setAssignedTo(assignedTo);
        }

        // We clear existing out and set new ones to let orphanRemoval handle deletions
        plan.getExercises().clear();
        plan.getExercises().addAll(mapRequestExercises(request.getExercises(), plan));

        WorkoutPlan updated = workoutPlanRepository.save(plan);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkoutPlanResponse getWorkoutPlanById(UUID currentUserId, UUID planId) {
        WorkoutPlan plan = workoutPlanRepository.findByIdWithExercises(planId)
                .orElseThrow(() -> new ResourceNotFoundException("PLAN_NOT_FOUND", "Workout plan not found"));

        boolean isCreator = plan.getCreatedBy().getId().equals(currentUserId);
        boolean isAssignee = plan.getAssignedTo() != null && plan.getAssignedTo().getId().equals(currentUserId);

        if (!isCreator && !isAssignee) {
            throw new ForbiddenException("You don't have access to this plan.");
        }

        return mapToResponse(plan);
    }

    private List<PlanExercise> mapRequestExercises(List<PlanExerciseRequest> requestExercises, WorkoutPlan plan) {
        if (requestExercises == null || requestExercises.isEmpty())
            return new ArrayList<>();
        return requestExercises.stream().map(req -> {
            Exercise exercise = exerciseRepository.findById(req.getExerciseId())
                    .orElseThrow(() -> new ResourceNotFoundException("EXERCISE_NOT_FOUND", "Exercise not found"));

            return PlanExercise.builder()
                    .workoutPlan(plan)
                    .exercise(exercise)
                    .sets(req.getSets() != null ? req.getSets() : 3)
                    .reps(req.getReps() != null ? req.getReps() : 10)
                    .restSeconds(req.getRestSeconds() != null ? req.getRestSeconds() : 60)
                    .orderIndex(req.getOrderIndex() != null ? req.getOrderIndex() : 0)
                    .notes(req.getNotes())
                    .build();
        }).collect(Collectors.toList());
    }

    private WorkoutPlanResponse mapToResponse(WorkoutPlan plan) {
        return WorkoutPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .planType(plan.getPlanType())
                .targetLevel(plan.getTargetLevel())
                .assignedToName(plan.getAssignedTo() != null ? plan.getAssignedTo().getFullName() : null)
                .createdByName(plan.getCreatedBy().getFullName())
                .scheduledDate(plan.getScheduledDate())
                .exercises(plan.getExercises().stream().map(ptEx -> PlanExerciseResponse.builder()
                        .orderIndex(ptEx.getOrderIndex())
                        .exerciseName(ptEx.getExercise().getName())
                        .muscleGroup(ptEx.getExercise().getMuscleGroup())
                        .sets(ptEx.getSets())
                        .reps(ptEx.getReps())
                        .restSeconds(ptEx.getRestSeconds())
                        .notes(ptEx.getNotes())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
