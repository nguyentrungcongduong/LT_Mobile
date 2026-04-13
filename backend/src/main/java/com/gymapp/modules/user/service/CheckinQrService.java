package com.gymapp.modules.user.service;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.user.dto.CheckinLogResponse;
import com.gymapp.modules.user.dto.QrTokenResponse;
import com.gymapp.modules.user.entity.CheckinLog;
import com.gymapp.modules.user.repository.CheckinLogRepository;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinQrService {

    private static final String QR_REDIS_PREFIX = "checkin:qr:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinLogRepository checkinLogRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // [1] GET /api/v1/checkin/qr — Generate QR token (60s TTL, stored in Redis)
    // ─────────────────────────────────────────────────────────────────────────

    public QrTokenResponse generateQrToken() {
        // Lấy user từ SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("UNAUTHORIZED", "Bạn chưa đăng nhập");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("USER_NOT_FOUND", "Không tìm thấy user"));

        if (!user.isActive()) {
            throw new BadRequestException("ACCOUNT_BLOCKED", "Tài khoản bị khóa");
        }

        if (user.getRole() != UserRole.USER) {
            throw new BadRequestException("FORBIDDEN_ROLE", "Chỉ USER mới được dùng tính năng check-in");
        }

        // Generate QR JWT token (60s TTL)
        String qrToken = jwtUtil.generateQrToken(email, user.getId(), user.getRole().name());

        // Parse jti từ token để lưu vào Redis
        Claims claims = jwtUtil.getClaimsFromToken(qrToken);
        String jti = claims.getId();

        long ttl = jwtUtil.getQrTokenExpirationSeconds();

        // Lưu jti vào Redis với TTL 60s — value = userId để có thể audit
        redisTemplate.opsForValue().set(
                QR_REDIS_PREFIX + jti,
                user.getId().toString(),
                ttl,
                TimeUnit.SECONDS
        );

        log.info("[QR_GEN] user={} jti={} ttl={}s", email, jti, ttl);

        return QrTokenResponse.builder()
                .qrToken(qrToken)
                .expiresInSeconds(ttl)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [2] POST /api/v1/checkin/verify — Verify QR token (JWT + Redis + membership + branch)
    //     [3] Xóa token Redis sau verify (one-time use)
    //     [4] Tạo CheckinLog record
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public CheckinLogResponse verifyQrToken(String qrToken, String branchIdStr) {

        // --- Step 1: Verify JWT signature & expiration ---
        Claims claims;
        try {
            claims = jwtUtil.getClaimsFromToken(qrToken);
        } catch (ExpiredJwtException e) {
            throw new BadRequestException("QR_EXPIRED", "Mã QR đã hết hạn (60 giây)");
        } catch (JwtException e) {
            throw new BadRequestException("QR_INVALID", "Mã QR không hợp lệ");
        }

        // --- Step 2: Validate type claim ---
        String type = claims.get("type", String.class);
        if (!"QR_CHECKIN".equals(type)) {
            throw new BadRequestException("QR_WRONG_TYPE", "Token không phải QR check-in");
        }

        String jti = claims.getId();
        String email = claims.getSubject();

        // --- Step 3: Kiểm tra Redis — token đã dùng hay chưa (one-time use) ---
        String redisKey = QR_REDIS_PREFIX + jti;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(exists)) {
            throw new BadRequestException("QR_USED_OR_EXPIRED", "Mã QR đã được sử dụng hoặc đã hết hạn");
        }

        // --- Step 4: Xóa token khỏi Redis ngay lập tức (one-time use) ---
        redisTemplate.delete(redisKey);
        log.info("[QR_VERIFY] Deleted Redis key={} for user={}", redisKey, email);

        // --- Step 5: Lấy user từ DB ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("USER_NOT_FOUND", "Không tìm thấy user"));

        if (!user.isActive()) {
            throw new BadRequestException("ACCOUNT_BLOCKED", "Tài khoản bị khóa");
        }

        if (user.getRole() != UserRole.USER) {
            throw new BadRequestException("FORBIDDEN_ROLE", "Chỉ USER mới được check-in");
        }

        // --- Step 6: Kiểm tra membership còn hiệu lực ---
        boolean hasValidMembership = membershipRepository
                .findActiveMembershipsByUserId(user.getId())
                .stream()
                .anyMatch(m -> !m.getEndDate().isBefore(LocalDate.now()));

        if (!hasValidMembership) {
            throw new BadRequestException("NO_VALID_MEMBERSHIP",
                    "Bạn chưa có gói tập còn hiệu lực. Vui lòng đăng ký gói tập.");
        }

        // --- Step 7: Kiểm tra đã check-in hôm nay chưa ---
        checkinLogRepository.findByUserIdAndCheckinDate(user.getId(), LocalDate.now())
                .ifPresent(existing -> {
                    throw new BadRequestException("ALREADY_CHECKED_IN",
                            "Bạn đã check-in hôm nay rồi!");
                });

        // --- Step 8: Validate branchId (nếu có) ---
        UUID branchId = null;
        if (branchIdStr != null && !branchIdStr.isBlank()) {
            try {
                branchId = UUID.fromString(branchIdStr);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("INVALID_BRANCH_ID", "branchId không hợp lệ");
            }
        }

        // --- Step 9: Lưu CheckinLog ---
        OffsetDateTime now = OffsetDateTime.now();
        CheckinLog checkinLog = CheckinLog.builder()
                .user(user)
                .branchId(branchId)
                .checkinDate(LocalDate.now())
                .checkinTime(now)
                .qrTokenJti(jti)
                .build();

        checkinLog = checkinLogRepository.save(checkinLog);
        log.info("[QR_VERIFY] Check-in success user={} branch={}", email, branchId);

        return toResponse(checkinLog);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [5] GET /api/v1/admin/checkin/logs — Admin xem lịch sử check-in
    // ─────────────────────────────────────────────────────────────────────────

    public Page<CheckinLogResponse> getAdminCheckinLogs(
            LocalDate date,
            UUID branchId,
            UUID userId,
            Pageable pageable) {

        Page<CheckinLog> page;

        if (date != null) {
            page = checkinLogRepository.findByCheckinDate(date, pageable);
        } else if (branchId != null) {
            page = checkinLogRepository.findByBranchId(branchId, pageable);
        } else if (userId != null) {
            page = checkinLogRepository.findByUserId(userId, pageable);
        } else {
            page = checkinLogRepository.findAllOrderByCheckinTimeDesc(pageable);
        }

        return page.map(this::toResponse);
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private CheckinLogResponse toResponse(CheckinLog log) {
        return CheckinLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userEmail(log.getUser().getEmail())
                .userFullName(log.getUser().getFullName())
                .branchId(log.getBranchId())
                .checkinDate(log.getCheckinDate())
                .checkinTime(log.getCheckinTime())
                .qrTokenJti(log.getQrTokenJti())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
