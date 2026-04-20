package com.gymapp.modules.training.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.training.dto.SaveWorkoutScheduleRequest;
import com.gymapp.modules.training.entity.WorkoutSchedule;
import com.gymapp.modules.training.repository.WorkoutScheduleRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API quản lý lịch tập hàng tuần của user.
 *
 * GET /api/v1/workout-schedules — lấy danh sách
 * PUT /api/v1/workout-schedules/{day} — lưu/cập nhật ngày
 * PUT /api/v1/workout-schedules/{day}/delete — xóa ngày
 */
@RestController
@RequestMapping("/api/v1/workout-schedules")
@RequiredArgsConstructor
public class WorkoutScheduleController {

    private final WorkoutScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ApiResponse<List<WorkoutScheduleDto>> getSchedules() {
        User user = getCurrentUser();
        List<WorkoutSchedule> schedules = scheduleRepository.findByUserId(user.getId());
        List<WorkoutScheduleDto> dtos = schedules.stream()
                .map(s -> new WorkoutScheduleDto(
                        s.getId().toString(),
                        s.getDayOfWeek(),
                        s.getRemindTime().toString()))
                .collect(Collectors.toList());
        return ApiResponse.ok(dtos, "Lấy lịch tập thành công");
    }

    @PutMapping("/{day}")
    public ApiResponse<WorkoutScheduleDto> saveSchedule(
            @PathVariable String day,
            @RequestBody SaveWorkoutScheduleRequest request) {

        User user = getCurrentUser();
        // Upsert: delete cũ nếu có → tạo mới
        scheduleRepository.deleteByUserIdAndDayOfWeek(user.getId(), day);

        LocalTime remindTime;
        try {
            remindTime = LocalTime.parse(request.getRemindTime().length() == 5
                    ? request.getRemindTime() + ":00"
                    : request.getRemindTime());
        } catch (Exception e) {
            remindTime = LocalTime.of(6, 0);
        }

        WorkoutSchedule schedule = WorkoutSchedule.builder()
                .user(user)
                .dayOfWeek(day.toUpperCase())
                .remindTime(remindTime)
                .build();
        schedule = scheduleRepository.save(schedule);

        return ApiResponse.ok(
                new WorkoutScheduleDto(schedule.getId().toString(), schedule.getDayOfWeek(),
                        schedule.getRemindTime().toString()),
                "Lưu lịch tập thành công");
    }

    @PutMapping("/{day}/delete")
    public ApiResponse<Void> deleteSchedule(@PathVariable String day) {
        User user = getCurrentUser();
        scheduleRepository.deleteByUserIdAndDayOfWeek(user.getId(), day.toUpperCase());
        return ApiResponse.ok(null, "Đã xóa lịch tập ngày " + day);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── inner DTO ─────────────────────────────────────────────────────────────
    record WorkoutScheduleDto(String id, String dayOfWeek, String remindTime) {
    }
}
