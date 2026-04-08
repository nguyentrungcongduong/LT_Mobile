package com.gymapp.modules.booking.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.booking.dto.ClientProgressDto;
import com.gymapp.modules.booking.dto.PtClientSummary;
import com.gymapp.modules.booking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pt/clients")
@RequiredArgsConstructor
public class PtClientController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('PT')")
    @GetMapping
    @Operation(summary = "Get PT clients")
    public ResponseEntity<ApiResponse<PageResponse<PtClientSummary>>> getPtClients(Pageable pageable) {
        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        PageResponse<PtClientSummary> response = bookingService.getPtClients(ptId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PreAuthorize("hasRole('PT')")
    @GetMapping("/{user_id}/progress")
    @Operation(summary = "Get client progress")
    public ResponseEntity<ApiResponse<ClientProgressDto>> getClientProgress(@PathVariable("user_id") UUID userId) {
        UUID ptId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));
        ClientProgressDto response = bookingService.getClientProgress(ptId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
