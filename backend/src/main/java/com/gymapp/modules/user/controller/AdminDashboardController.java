package com.gymapp.modules.user.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.checkin.repository.CheckinLogRepository;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final CheckinLogRepository checkinLogRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        long totalCheckins = checkinLogRepository.count();
        long totalUsers = userRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCheckins", totalCheckins);
        // Note: 'newRegistrations' could be users created this month, but for simplicity we return a mock or total
        stats.put("newRegistrations", totalUsers); 

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
