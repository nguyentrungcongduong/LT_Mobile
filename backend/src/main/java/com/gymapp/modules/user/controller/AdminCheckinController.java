package com.gymapp.modules.checkin.controller;

import com.gymapp.modules.checkin.dto.CheckinLogResponse;
import com.gymapp.modules.checkin.service.CheckinQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController("adminCheckinQrController")
@RequestMapping("/api/v1/admin/checkin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCheckinController {

    private final CheckinQrService checkinQrService;

    /**
     * [5] GET /api/v1/admin/checkin/logs
     * Admin xem lịch sử check-in, hỗ trợ filter theo:
     *   - date (yyyy-MM-dd)
     *   - branchId (UUID)
     *   - userId (UUID)
     *   - page, size (pagination)
     *
     * Ví dụ:
     *   GET /api/v1/admin/checkin/logs?page=0&size=20
     *   GET /api/v1/admin/checkin/logs?date=2026-04-13
     *   GET /api/v1/admin/checkin/logs?branchId=<UUID>
     *   GET /api/v1/admin/checkin/logs?userId=<UUID>
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<CheckinLogResponse>> getCheckinLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID userId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CheckinLogResponse> result = checkinQrService.getAdminCheckinLogs(
                date, branchId, userId, pageable);
        return ResponseEntity.ok(result);
    }
}
