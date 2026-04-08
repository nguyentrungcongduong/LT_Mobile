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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            Pageable pageable) {
        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        PageResponse<BookingSummary> response = bookingService.getPtBookings(ptId, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
