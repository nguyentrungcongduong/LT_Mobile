package com.gymapp.modules.training.repository;

import com.gymapp.modules.training.entity.WorkoutSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkoutScheduleRepository extends JpaRepository<WorkoutSchedule, UUID> {

    /**
     * Tìm tất cả workout schedule theo user
     */
    List<WorkoutSchedule> findByUserId(UUID userId);

    /**
     * Tìm tất cả schedule có lịch tập vào một ngày cụ thể trong tuần (MON, TUE, ...)
     * Dùng cho scheduler hàng ngày để gửi FCM push.
     */
    @Query("SELECT ws FROM WorkoutSchedule ws " +
           "JOIN FETCH ws.user u " +
           "WHERE ws.dayOfWeek = :dayOfWeek " +
           "AND u.isActive = true")
    List<WorkoutSchedule> findByDayOfWeekWithUser(@Param("dayOfWeek") String dayOfWeek);

    /**
     * Xoá schedule của user theo ngày
     */
    void deleteByUserIdAndDayOfWeek(UUID userId, String dayOfWeek);
}
