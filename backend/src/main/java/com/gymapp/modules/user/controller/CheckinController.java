package com.gymapp.modules.checkin.controller;

import com.gymapp.modules.checkin.dto.CheckinLogResponse;
import com.gymapp.modules.checkin.dto.CheckinVerifyRequest;
import com.gymapp.modules.checkin.dto.QrTokenResponse;
import com.gymapp.modules.checkin.service.CheckinQrService;
import com.gymapp.modules.checkin.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController("checkinQrController")
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;
    private final CheckinQrService checkinQrService;

    /**
     * [1] GET /api/v1/checkin/qr
     * User gọi để lấy QR token (JWT, 60s TTL, stored in Redis).
     * QR code được gen phía client từ token string này.
     */
    @GetMapping("/qr")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<QrTokenResponse> generateQrToken() {
        return ResponseEntity.ok(checkinQrService.generateQrToken());
    }

    /**
     * [2] POST /api/v1/checkin/verify
     * Staff/system quét QR → gửi token lên để verify.
     * Flow: JWT verify → Redis one-time check → membership → save CheckinLog.
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
     * [Legacy] POST /api/v1/checkin
     * API check-in cũ (giữ lại để không break mobile client cũ).
     */
    @PostMapping
    public ResponseEntity<?> checkin(@RequestBody com.gymapp.modules.checkin.dto.CheckinRequest request) {
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

