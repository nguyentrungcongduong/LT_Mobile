package com.gymapp.modules.booking.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.booking.dto.BookingSummary;
import com.gymapp.modules.booking.dto.CancelBookingResponse;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Bookings", description = "Admin booking management")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ApiResponse<PageResponse<BookingSummary>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String ptName,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            Pageable pageable) {

        ptName = (ptName == null || ptName.trim().isEmpty()) ? null : ptName;
        search = (search == null || search.trim().isEmpty()) ? null : search;

        return ApiResponse.ok(
                bookingService.getAllBookingsPaginated(status, ptName, search, fromDate, toDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking detail")
    public ResponseEntity<ApiResponse<BookingSummary>> getBookingDetail(@PathVariable UUID id) {
        return ResponseEntity.notFound().build();
    }

    /** Admin hủy booking → hoàn tiền tự động PROCESSED */
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Admin cancel booking with auto refund")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> adminCancelBooking(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {

        UUID adminId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Chưa đăng nhập")));

        String reason = (body != null && body.containsKey("reason") && !body.get("reason").isBlank())
                ? body.get("reason")
                : "Admin hủy lịch";

        CancelBookingResponse response = bookingService.adminCancelBooking(adminId, id, reason);
        return ResponseEntity.ok(ApiResponse.ok(response, "Đã hủy booking thành công"));
    }
}
