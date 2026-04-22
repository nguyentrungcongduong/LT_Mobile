package com.gymapp.modules.user.service;

import com.gymapp.modules.user.dto.request.PtProfileCreateReq;
import com.gymapp.modules.user.dto.request.PtProfileUpdateReq;
import com.gymapp.modules.user.dto.request.SuspendReq;
import com.gymapp.modules.user.dto.response.PtDetailDto;
import com.gymapp.modules.user.dto.response.PtListDto;
import com.gymapp.modules.user.dto.response.PtProfileDto;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import com.gymapp.common.response.PageResponse;

import com.gymapp.modules.user.dto.request.CreateReviewRequest;
import com.gymapp.modules.user.dto.response.ReviewDto;

public interface PtProfileService {
    PtProfileDto createProfile(UUID userId, PtProfileCreateReq req);
    PtProfileDto updateProfile(UUID userId, PtProfileUpdateReq req);
    PtProfileDto getMyProfile(UUID userId);  // PT xem profile + stats của chính mình
    
    PageResponse<PtListDto> getPtList(String specialization, BigDecimal minRating, BigDecimal maxPrice, Pageable pageable, boolean restrictToApproved);
    PtDetailDto getPtDetail(UUID ptId, boolean restrictToApproved);
    
    void approvePt(UUID adminId, UUID ptId);
    void suspendPt(UUID adminId, UUID ptId, SuspendReq req);

    ReviewDto submitReview(UUID userId, UUID ptId, CreateReviewRequest req);
    boolean hasUserReviewed(UUID userId, UUID ptId);
}
