package com.gymapp.modules.booking.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.booking.dto.BookingSummary;
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
        // TODO: Implement when needed
        return ResponseEntity.notFound().build();
    }
}
