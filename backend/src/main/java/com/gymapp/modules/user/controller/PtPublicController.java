package com.gymapp.modules.user.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.security.UserDetailsImpl;
import com.gymapp.modules.user.dto.request.CreateReviewRequest;
import com.gymapp.modules.user.dto.response.PtDetailDto;
import com.gymapp.modules.user.dto.response.PtListDto;
import com.gymapp.modules.user.dto.response.ReviewDto;
import com.gymapp.modules.user.service.PtProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
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

        PtDetailDto result = ptProfileService.getPtDetail(ptId, isNotAdmin);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ── Review Endpoints ─────────────────────────────────────────────────────

    /**
     * POST /api/v1/pts/{pt_id}/reviews
     * Submit a star rating + comment for a PT. Requires authentication.
     */
    @PostMapping("/{pt_id}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> submitReview(
            @PathVariable("pt_id") UUID ptId,
            @RequestBody @Valid CreateReviewRequest req,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        UUID userId = userDetails.getId();
        ReviewDto result = ptProfileService.submitReview(userId, ptId, req);
        return ResponseEntity.ok(ApiResponse.ok(result, "Đánh giá thành công!"));
    }

    /**
     * GET /api/v1/pts/{pt_id}/reviews/me
     * Check if the current user has already reviewed this PT.
     */
    @GetMapping("/{pt_id}/reviews/me")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkMyReview(
            @PathVariable("pt_id") UUID ptId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of("hasReviewed", false)));
        }
        boolean hasReviewed = ptProfileService.hasUserReviewed(userDetails.getId(), ptId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("hasReviewed", hasReviewed)));
    }
}
