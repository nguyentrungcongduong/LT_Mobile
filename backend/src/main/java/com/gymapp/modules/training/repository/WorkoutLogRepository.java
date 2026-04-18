package com.gymapp.modules.training.repository;

import com.gymapp.modules.training.entity.WorkoutLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, UUID> {
    @Query("SELECT wl FROM WorkoutLog wl LEFT JOIN FETCH wl.workoutPlan WHERE wl.user.id = :userId AND wl.logDate >= :fromDate AND wl.logDate <= :toDate ORDER BY wl.logDate DESC")
    Page<WorkoutLog> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
