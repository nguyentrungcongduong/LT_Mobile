package com.gymapp.modules.booking.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.booking.dto.*;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    @Operation(summary = "Create booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        UUID userId = getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        BookingResponse response = bookingService.createBooking(userId, request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Booking created successfully"));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/batch")
    @Operation(summary = "Create multiple bookings and pay once")
    public ResponseEntity<ApiResponse<BatchBookingResponse>> createBatchBookings(
            @Valid @RequestBody BatchBookingRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        UUID userId = getCurrentUserId();
        String ipAddress = httpRequest.getRemoteAddr();
        BatchBookingResponse response = bookingService.createBatchBookings(userId, request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Batch bookings created successfully"));
    }

    @PreAuthorize("hasAnyRole('USER', 'PT')")
    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking")
    public ResponseEntity<ApiResponse<CancelBookingResponse>> cancelBooking(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody CancelBookingRequest request) {
        UUID userId = getCurrentUserId();
        CancelBookingResponse response = bookingService.cancelBooking(userId, id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Booking cancelled successfully"));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    @Operation(summary = "Get user bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingSummary>>> getUserBookings(
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable) {
        UUID userId = getCurrentUserId();
        PageResponse<BookingSummary> response = bookingService.getUserBookings(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private UUID getCurrentUserId() {
        return UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/alls")
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }
}
