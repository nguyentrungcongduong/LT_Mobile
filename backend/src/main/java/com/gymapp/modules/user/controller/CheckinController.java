package com.gymapp.modules.user.controller;

import com.gymapp.modules.user.dto.response.CheckinLogResponse;
import com.gymapp.modules.user.dto.response.CheckinStatsResponse;
import com.gymapp.modules.user.dto.request.CheckinVerifyRequest;
import com.gymapp.modules.user.dto.request.CheckinRequest;
import com.gymapp.modules.user.dto.response.QrTokenResponse;
import com.gymapp.modules.user.service.CheckinQrService;
import com.gymapp.modules.user.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("checkinQrController")
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final CheckinQrService checkinQrService;

    /**
     * [1] GET /api/v1/checkin/qr
     * User gọi để lấy QR token (JWT, 60s TTL, stored in-memory).
     */
    @GetMapping("/qr")
    @PreAuthorize("hasRole('USER') or hasRole('PT')")
    public ResponseEntity<QrTokenResponse> generateQrToken() {
        return ResponseEntity.ok(checkinQrService.generateQrToken());
    }

    /**
     * [2] POST /api/v1/checkin/verify
     * Admin quét QR → verify token → ghi log check-in.
     */
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckinLogResponse> verifyQrToken(
            @RequestBody CheckinVerifyRequest request) {
        CheckinLogResponse response = checkinQrService.verifyQrToken(
                request.getQrToken(),
                request.getBranchId()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * [3] GET /api/v1/checkin/stats
     * User xem thống kê cá nhân: tổng buổi, streak, tổng giờ.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('PT')")
    public ResponseEntity<CheckinStatsResponse> getMyStats() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(checkinQrService.getMyCheckinStats(userId));
    }

    /**
     * [Legacy] POST /api/v1/checkin
     * API check-in cũ (giữ lại để không break mobile client cũ).
     */
    @PostMapping
    public ResponseEntity<?> checkin(@RequestBody CheckinRequest request) {
        try {
            String result = checkinService.checkin(request.getQrData());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            if ("Unauthorized".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
