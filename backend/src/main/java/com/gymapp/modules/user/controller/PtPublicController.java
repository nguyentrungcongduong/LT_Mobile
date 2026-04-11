package com.gymapp.modules.user.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.security.UserDetailsImpl;
import com.gymapp.modules.user.dto.response.PtDetailDto;
import com.gymapp.modules.user.dto.response.PtListDto;
import com.gymapp.modules.user.service.PtProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

import com.gymapp.common.response.PageResponse;

@RestController
@RequestMapping("/api/v1/pts")
@RequiredArgsConstructor
public class PtPublicController {

    private final PtProfileService ptProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PtListDto>>> getPts(
            @RequestParam(required = false) String specialization,
            @RequestParam(name = "min_rating", required = false) BigDecimal minRating,
            @RequestParam(name = "max_price", required = false) BigDecimal maxPrice,
            @PageableDefault(sort = "ratingAvg", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean isNotAdmin = userDetails == null || userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Only return approved PTs unless user is ADMIN
        PageResponse<PtListDto> results = ptProfileService.getPtList(specialization, minRating, maxPrice, pageable,
                isNotAdmin);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/{pt_id}")
    public ResponseEntity<ApiResponse<PtDetailDto>> getPtDetail(
            @PathVariable("pt_id") UUID ptId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean isNotAdmin = userDetails == null || userDetails.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Restrict to approved PTs unless user is ADMIN
        PtDetailDto result = ptProfileService.getPtDetail(ptId, isNotAdmin);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
