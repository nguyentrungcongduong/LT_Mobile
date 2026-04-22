package com.gymapp.modules.user.service.impl;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ConflictException;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.user.dto.request.CreateReviewRequest;
import com.gymapp.modules.user.dto.request.PtProfileCreateReq;
import com.gymapp.modules.user.dto.request.PtProfileUpdateReq;
import com.gymapp.modules.user.dto.request.SuspendReq;
import com.gymapp.modules.user.dto.response.PtDetailDto;
import com.gymapp.modules.user.dto.response.PtListDto;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.user.dto.response.PtProfileDto;
import com.gymapp.modules.user.dto.response.ReviewDto;
import com.gymapp.modules.user.entity.PtProfile;
import com.gymapp.modules.user.entity.PtReview;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.PtProfileRepository;
import com.gymapp.modules.user.repository.PtProfileSpecification;
import com.gymapp.modules.user.repository.PtReviewRepository;
import com.gymapp.modules.user.repository.UserRepository;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.repository.BookingRepository;
import com.gymapp.modules.user.service.PtProfileService;
import com.gymapp.modules.branch.repository.BranchRepository;
import com.gymapp.modules.branch.entity.Branch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PtProfileServiceImpl implements PtProfileService {

    private final PtProfileRepository ptProfileRepository;
    private final UserRepository userRepository;
    private final PtReviewRepository ptReviewRepository;
    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional
    public PtProfileDto createProfile(UUID userId, PtProfileCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        if (!user.getRole().equals(UserRole.PT)) {
            throw new BadRequestException("INVALID_ROLE", "Người dùng phải có vai trò PT để tạo hồ sơ");
        }

        if (ptProfileRepository.findByUserId(userId).isPresent()) {
            throw new ConflictException("PROFILE_EXISTS", "Hồ sơ PT đã tồn tại cho người dùng này");
        }

        PtProfile profile = PtProfile.builder()
                .user(user)
                .bio(req.getBio())
                .specializations(new ArrayList<>(req.getSpecializations()))
                .pricePerSession(req.getPricePerSession())
                .yearsExperience(req.getYearsExperience())
                .certificateUrls(req.getCertificateUrls() != null ? new ArrayList<>(req.getCertificateUrls())
                        : new ArrayList<>())
                .isApproved(false)
                .ratingAvg(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        // Gán chi nhánh nếu có truyền branchId
        if (req.getBranchId() != null) {
            Branch branch = branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("BRANCH_NOT_FOUND", "Không tìm thấy chi nhánh"));
            profile.setBranch(branch);
        }

        PtProfile saved = ptProfileRepository.save(profile);
        return mapToEventDto(saved);
    }

    @Override
    @Transactional
    public PtProfileDto updateProfile(UUID userId, PtProfileUpdateReq req) {
        PtProfile profile = ptProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ PT"));

        if (req.getBio() != null)
            profile.setBio(req.getBio());
        if (req.getSpecializations() != null)
            profile.setSpecializations(new ArrayList<>(req.getSpecializations()));
        if (req.getPricePerSession() != null)
            profile.setPricePerSession(req.getPricePerSession());
        if (req.getYearsExperience() != null)
            profile.setYearsExperience(req.getYearsExperience());
        if (req.getCertificateUrls() != null)
            profile.setCertificateUrls(new ArrayList<>(req.getCertificateUrls()));
        // Cập nhật chi nhánh nếu có truyền
        if (req.getBranchId() != null) {
            Branch branch = branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("BRANCH_NOT_FOUND", "Không tìm thấy chi nhánh"));
            profile.setBranch(branch);
        }

        PtProfile saved = ptProfileRepository.save(profile);
        return mapToEventDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PtProfileDto getMyProfile(UUID userId) {
        PtProfile profile = ptProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PROFILE_NOT_FOUND", "Chưa có hồ sơ PT"));

        List<BookingStatus> activeStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING);
        long totalClients  = bookingRepository.countDistinctClientsByPtId(userId, activeStatuses);
        long totalSessions = bookingRepository.countByPtIdAndStatus(userId, BookingStatus.CONFIRMED);

        return PtProfileDto.builder()
                .id(profile.getId())
                .userId(userId)
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .pricePerSession(profile.getPricePerSession())
                .ratingAvg(profile.getRatingAvg())
                .totalReviews(profile.getTotalReviews())
                .totalClients(totalClients)
                .totalSessions(totalSessions)
                .yearsExperience(profile.getYearsExperience())
                .certificateUrls(profile.getCertificateUrls())
                .isApproved(profile.isApproved())
                .approvedAt(profile.getApprovedAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PtListDto> getPtList(String specialization, BigDecimal minRating, BigDecimal maxPrice,
            Pageable pageable, boolean restrictToApproved) {
        Boolean approvedFilter = restrictToApproved ? true : null;
        Specification<PtProfile> spec = PtProfileSpecification.getFilterSpecification(specialization, minRating,
                maxPrice, approvedFilter);

        Page<PtProfile> page = ptProfileRepository.findAll(spec, pageable);
        List<PtListDto> items = page.getContent().stream().map(profile -> PtListDto.builder()
                .id(profile.getId())
                .fullName(profile.getUser().getFullName())
                .avatarUrl(profile.getUser().getAvatarUrl())
                .specializations(profile.getSpecializations())
                .pricePerSession(profile.getPricePerSession())
                .ratingAvg(profile.getRatingAvg())
                .totalReviews(profile.getTotalReviews())
                .yearsExperience(profile.getYearsExperience())
                .isApproved(profile.isApproved())
                .build()).collect(Collectors.toList());

        return PageResponse.<PtListDto>builder()
                .items(items)
                .pagination(PageResponse.PaginationMeta.builder()
                        .page(page.getNumber() + 1)
                        .limit(page.getSize())
                        .total(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PtDetailDto getPtDetail(UUID ptId, boolean restrictToApproved) {
        PtProfile profile = ptProfileRepository.findById(ptId)
                .orElseThrow(() -> new ResourceNotFoundException("PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ PT"));

        if (restrictToApproved && !profile.isApproved()) {
            throw new ResourceNotFoundException("PROFILE_NOT_FOUND_OR_UNAPPROVED",
                    "Không tìm thấy hồ sơ PT hoặc chưa được phê duyệt");
        }

        return PtDetailDto.builder()
                .id(profile.getId())
                .fullName(profile.getUser().getFullName())
                .avatarUrl(profile.getUser().getAvatarUrl())
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .pricePerSession(profile.getPricePerSession())
                .ratingAvg(profile.getRatingAvg())
                .totalReviews(profile.getTotalReviews())
                .yearsExperience(profile.getYearsExperience())
                .certificateUrls(profile.getCertificateUrls())
                .reviews(ptReviewRepository.findByPtIdOrderByCreatedAtDesc(ptId).stream()
                        .map(r -> ReviewDto.builder()
                                .id(r.getId())
                                .userId(r.getUser().getId())
                                .userName(r.getUser().getFullName())
                                .avatarUrl(r.getUser().getAvatarUrl())
                                .rating(r.getRating().intValue())
                                .comment(r.getComment())
                                .createdAt(r.getCreatedAt())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public ReviewDto submitReview(UUID userId, UUID ptId, CreateReviewRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.gymapp.common.exception.ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        PtProfile pt = ptProfileRepository.findById(ptId)
                .orElseThrow(() -> new com.gymapp.common.exception.ResourceNotFoundException("PT_NOT_FOUND", "Không tìm thấy PT"));

        if (ptReviewRepository.existsByUserIdAndPtId(userId, ptId)) {
            throw new com.gymapp.common.exception.ConflictException("ALREADY_REVIEWED", "Bạn đã đánh giá PT này rồi");
        }

        PtReview review = PtReview.builder()
                .user(user)
                .pt(pt)
                .rating(req.getRating().shortValue())
                .comment(req.getComment())
                .build();

        PtReview saved = ptReviewRepository.saveAndFlush(review);

        // Recalculate avg rating
        Double avg = ptReviewRepository.calculateAvgRating(ptId);
        long count = ptReviewRepository.countByPtId(ptId);
        pt.setRatingAvg(java.math.BigDecimal.valueOf(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0));
        pt.setTotalReviews((int) count);
        ptProfileRepository.save(pt);

        return ReviewDto.builder()
                .id(saved.getId())
                .userId(userId)
                .userName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .rating(req.getRating())
                .comment(req.getComment())
                .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt() : OffsetDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewed(UUID userId, UUID ptId) {
        return ptReviewRepository.existsByUserIdAndPtId(userId, ptId);
    }

    @Override
    @Transactional
    public void approvePt(UUID adminId, UUID ptId) {
        PtProfile profile = ptProfileRepository.findById(ptId)
                .orElseThrow(() -> new ResourceNotFoundException("PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ PT"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN_NOT_FOUND", "Không tìm thấy admin"));

        if (profile.isApproved()) {
            throw new ConflictException("PROFILE_ALREADY_APPROVED", "Hồ sơ PT đã được phê duyệt");
        }

        profile.setApproved(true);
        profile.setApprovedAt(OffsetDateTime.now());
        profile.setApprovedBy(admin);

        ptProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void suspendPt(UUID adminId, UUID ptId, SuspendReq req) {
        PtProfile profile = ptProfileRepository.findById(ptId)
                .orElseThrow(() -> new ResourceNotFoundException("PROFILE_NOT_FOUND", "Không tìm thấy hồ sơ PT"));

        if (!profile.isApproved()) {
            throw new ConflictException("PROFILE_NOT_APPROVED", "Hồ sơ PT chưa được phê duyệt, không thể đình chỉ");
        }

        profile.setApproved(false);
        // Note: For full audit logging we'll also log the req.getReason() into a
        // separate audit table if it existed,
        // since 'suspend' removes approval, we simply set to false.

        ptProfileRepository.save(profile);
    }

    private PtProfileDto mapToEventDto(PtProfile profile) {
        return PtProfileDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .branchId(profile.getBranch() != null ? profile.getBranch().getId() : null)
                .branchName(profile.getBranch() != null ? profile.getBranch().getName() : null)
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .pricePerSession(profile.getPricePerSession())
                .ratingAvg(profile.getRatingAvg())
                .totalReviews(profile.getTotalReviews())
                .yearsExperience(profile.getYearsExperience())
                .certificateUrls(profile.getCertificateUrls())
                .isApproved(profile.isApproved())
                .approvedAt(profile.getApprovedAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
