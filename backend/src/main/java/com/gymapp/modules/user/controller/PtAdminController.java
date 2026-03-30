package com.gymapp.modules.user.controller;

import com.gymapp.common.response.ApiResponse;

import com.gymapp.modules.user.dto.request.SuspendReq;
import com.gymapp.modules.user.service.PtProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/pts")
@RequiredArgsConstructor
public class PtAdminController {

    private final PtProfileService ptProfileService;

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{pt_id}/approve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approvePt(
            @PathVariable("pt_id") UUID ptId) {
        
        UUID adminId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Không tìm thấy thông tin đăng nhập")));
        ptProfileService.approvePt(adminId, ptId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("is_approved", true);
        
        return ResponseEntity.ok(ApiResponse.ok(result, "PT approved successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{pt_id}/suspend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> suspendPt(
            @PathVariable("pt_id") UUID ptId,
            @Valid @RequestBody SuspendReq req) {
        
        UUID adminId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Không tìm thấy thông tin đăng nhập")));
        ptProfileService.suspendPt(adminId, ptId, req);
        
        Map<String, Object> result = new HashMap<>();
        result.put("is_approved", false);
        
        return ResponseEntity.ok(ApiResponse.ok(result, "PT suspended successfully"));
    }
}
