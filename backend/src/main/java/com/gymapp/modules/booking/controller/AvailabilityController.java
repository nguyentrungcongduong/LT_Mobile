package com.gymapp.modules.booking.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.booking.dto.PtAvailabilityRequest;
import com.gymapp.modules.booking.dto.PtAvailabilityResponse;
import com.gymapp.modules.booking.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PreAuthorize("hasAnyRole('USER', 'PT', 'ADMIN')")
    @GetMapping("/pts/{ptId}/availability")
    @Operation(summary = "Get PT availability")
    public ResponseEntity<ApiResponse<List<PtAvailabilityResponse>>> getPtAvailability(
            @PathVariable(name = "ptId") UUID ptId,
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<PtAvailabilityResponse> response = availabilityService.getPtAvailability(ptId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PreAuthorize("hasRole('PT')")
    @PostMapping("/pt/availability")
    @Operation(summary = "PT set availability")
    public ResponseEntity<ApiResponse<PtAvailabilityResponse>> createAvailability(
            @Valid @RequestBody PtAvailabilityRequest request) {

        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));

        PtAvailabilityResponse response = availabilityService.createAvailability(ptId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
