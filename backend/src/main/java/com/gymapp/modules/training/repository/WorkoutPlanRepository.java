package com.gymapp.modules.training.repository;

import com.gymapp.modules.training.entity.WorkoutPlan;
import com.gymapp.modules.training.enums.WpType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

import com.gymapp.modules.training.enums.TargetLevel;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isActive = true AND w.planType = :type AND (w.createdBy.id = :userId OR w.assignedTo.id = :userId)")
    Page<WorkoutPlan> findAllByTypeAndUser(@Param("type") WpType type, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT w FROM WorkoutPlan w WHERE w.isActive = true AND w.targetLevel = :targetLevel AND w.planType = :planType")
    Page<WorkoutPlan> findRecommendedPlans(@Param("targetLevel") TargetLevel targetLevel, @Param("planType") WpType planType, Pageable pageable);

    @Query("SELECT w FROM WorkoutPlan w LEFT JOIN FETCH w.exercises e LEFT JOIN FETCH e.exercise WHERE w.id = :id AND w.isActive = true")
    Optional<WorkoutPlan> findByIdWithExercises(@Param("id") UUID id);
}
