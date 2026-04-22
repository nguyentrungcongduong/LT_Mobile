package com.gymapp.modules.booking.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.booking.dto.BookingSummary;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pt/bookings")
@RequiredArgsConstructor
public class PtBookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('PT')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingSummary>>> getPtBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(value = "upcoming_only", required = false) Boolean upcomingOnly,
            Pageable pageable) {
        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        PageResponse<BookingSummary> response = bookingService.getPtBookings(ptId, status, upcomingOnly, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * PT xác nhận học viên có tham gia buổi tập hay không.
     * Body: { "attended": true/false }
     * true  → COMPLETED (PT nhận tiền)
     * false → NO_SHOW  (user mất tiền, PT không nhận tiền)
     */
    @PreAuthorize("hasRole('PT')")
    @PutMapping("/{bookingId}/attendance")
    public ResponseEntity<ApiResponse<Void>> confirmAttendance(
            @PathVariable UUID bookingId,
            @RequestBody Map<String, Boolean> body) {
        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        Boolean attended = body.get("attended");
        if (attended == null) {
            throw new com.gymapp.common.exception.BadRequestException("MISSING_FIELD",
                    "Trường 'attended' là bắt buộc");
        }
        bookingService.confirmAttendance(ptId, bookingId, attended);
        String msg = attended ? "Xác nhận học viên đã tập thành công" : "Đã đánh dấu vắng mặt";
        return ResponseEntity.ok(ApiResponse.ok(null, msg));
    }
}
