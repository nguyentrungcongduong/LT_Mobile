package com.gymapp.modules.user.controller;

import com.gymapp.common.response.ApiResponse;

import com.gymapp.modules.user.dto.request.PtProfileCreateReq;
import com.gymapp.modules.user.dto.request.PtProfileUpdateReq;
import com.gymapp.modules.user.dto.response.PtProfileDto;
import com.gymapp.modules.user.service.PtProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pt/profile")
@RequiredArgsConstructor
public class PtProfileController {

    private final PtProfileService ptProfileService;

    @PreAuthorize("hasRole('PT')")
    @PostMapping
    public ResponseEntity<ApiResponse<PtProfileDto>> createProfile(
            @Valid @RequestBody PtProfileCreateReq req) {
        
        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Không tìm thấy thông tin đăng nhập")));
        PtProfileDto response = ptProfileService.createProfile(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "Profile created successfully"));
    }

    @PreAuthorize("hasRole('PT')")
    @PutMapping
    public ResponseEntity<ApiResponse<PtProfileDto>> updateProfile(
            @Valid @RequestBody PtProfileUpdateReq req) {
        
        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Không tìm thấy thông tin đăng nhập")));
        PtProfileDto response = ptProfileService.updateProfile(userId, req);
        return ResponseEntity.ok(ApiResponse.ok(response, "Profile updated successfully"));
    }
}
