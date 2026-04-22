package com.gymapp.modules.user.service;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.user.dto.response.CheckinLogResponse;
import com.gymapp.modules.user.dto.response.CheckinStatsResponse;
import com.gymapp.modules.user.dto.response.QrTokenResponse;
import com.gymapp.modules.user.entity.CheckinLog;
import com.gymapp.modules.user.repository.CheckinLogRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CheckinQrService {

    // In-memory QR token store: jti -> userId string
    // Used instead of Redis for demo purposes
    private static final Map<String, String> QR_TOKEN_STORE = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService CLEANER = Executors.newSingleThreadScheduledExecutor();

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinLogRepository checkinLogRepository;

    public CheckinQrService(JwtUtil jwtUtil,
                             UserRepository userRepository,
                             MembershipRepository membershipRepository,
                             CheckinLogRepository checkinLogRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.checkinLogRepository = checkinLogRepository;
    }

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

        if (user.getRole() != UserRole.USER && user.getRole() != UserRole.PT) {
            throw new BadRequestException("FORBIDDEN_ROLE", "Chỉ USER và PT mới được dùng tính năng check-in");
        }

        // Generate QR JWT token
        String qrToken = jwtUtil.generateQrToken(email, user.getId(), user.getRole().name());

        // Parse jti
        Claims claims = jwtUtil.getClaimsFromToken(qrToken);
        String jti = claims.getId();
        long ttl = jwtUtil.getQrTokenExpirationSeconds();

        // Store in-memory with auto-expiry
        QR_TOKEN_STORE.put(jti, user.getId().toString());
        CLEANER.schedule(() -> QR_TOKEN_STORE.remove(jti), ttl, TimeUnit.SECONDS);

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

        // --- Step 3: Kiểm tra in-memory store — token đã dùng hay chưa (one-time use) ---
        if (!QR_TOKEN_STORE.containsKey(jti)) {
            throw new BadRequestException("QR_USED_OR_EXPIRED", "Mã QR đã được sử dụng hoặc đã hết hạn");
        }

        // --- Step 4: Xóa token khỏi store ngay lập tức (one-time use) ---
        QR_TOKEN_STORE.remove(jti);
        log.info("[QR_VERIFY] Removed token jti={} for user={}", jti, email);

        // --- Step 5: Lấy user từ DB ---
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("USER_NOT_FOUND", "Không tìm thấy user"));

        if (!user.isActive()) {
            throw new BadRequestException("ACCOUNT_BLOCKED", "Tài khoản bị khóa");
        }

        if (user.getRole() != UserRole.USER && user.getRole() != UserRole.PT) {
            throw new BadRequestException("FORBIDDEN_ROLE", "Chỉ USER và PT mới được check-in");
        }

        // --- Step 6: Kiểm tra membership còn hiệu lực (chỉ với USER, PT không cần) ---
        if (user.getRole() == UserRole.USER) {
            boolean hasValidMembership = membershipRepository
                    .findActiveMembershipsByUserId(user.getId())
                    .stream()
                    .anyMatch(m -> !m.getEndDate().isBefore(LocalDate.now()));

            if (!hasValidMembership) {
                throw new BadRequestException("NO_VALID_MEMBERSHIP",
                        "Bạn chưa có gói tập còn hiệu lực. Vui lòng đăng ký gói tập.");
            }
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
                .branch(null)   // branchId không bắt buộc; để null nếu không có branch entity
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

    @Transactional(readOnly = true)
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

    // ─────────────────────────────────────────────────────────────────────────
    // [5b] GET /api/v1/admin/checkin/export — Admin xuất CSV
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportCheckinCsv(LocalDate date, UUID branchId, UUID userId) {
        Pageable all = org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE);
        Page<CheckinLog> page;
        if (date != null) {
            page = checkinLogRepository.findByCheckinDate(date, all);
        } else if (branchId != null) {
            page = checkinLogRepository.findByBranchId(branchId, all);
        } else if (userId != null) {
            page = checkinLogRepository.findByUserId(userId, all);
        } else {
            page = checkinLogRepository.findAllOrderByCheckinTimeDesc(all);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID,UserEmail,UserFullName,CheckinDate,CheckinTime,BranchId\n");
        for (CheckinLog log : page.getContent()) {
            sb.append(log.getId()).append(",")
              .append(log.getUser().getEmail()).append(",")
              .append(log.getUser().getFullName()).append(",")
              .append(log.getCheckinDate()).append(",")
              .append(log.getCheckinTime()).append(",")
              .append(log.getBranch() != null ? log.getBranch().getId() : "").append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // [6] GET /api/v1/checkin/stats — User xem thống kê cá nhân
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CheckinStatsResponse getMyCheckinStats(UUID userId) {
        long totalSessions = checkinLogRepository.countByUserId(userId);

        // Tính streak: đếm số ngày liên tiếp gần nhất (hôm nay hoặc hôm qua tính là bắt đầu)
        List<LocalDate> dates = checkinLogRepository.findDistinctCheckinDatesByUserId(userId);
        int streak = 0;
        if (!dates.isEmpty()) {
            LocalDate today = LocalDate.now();
            // Cho phép bắt đầu từ hôm nay hoặc hôm qua
            LocalDate expected = dates.get(0).equals(today) || dates.get(0).equals(today.minusDays(1))
                    ? dates.get(0) : null;
            if (expected != null) {
                for (LocalDate d : dates) {
                    if (d.equals(expected)) {
                        streak++;
                        expected = expected.minusDays(1);
                    } else {
                        break;
                    }
                }
            }
        }

        double totalHours = Math.round(totalSessions * 1.5 * 10.0) / 10.0;

        return CheckinStatsResponse.builder()
                .totalSessions(totalSessions)
                .streakDays(streak)
                .totalHours(totalHours)
                .build();
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private CheckinLogResponse toResponse(CheckinLog log) {
        return CheckinLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userEmail(log.getUser().getEmail())
                .userFullName(log.getUser().getFullName())
                .branchId(log.getBranch() != null ? log.getBranch().getId() : null)
                .branchName(log.getBranch() != null ? log.getBranch().getName() : null)
                .checkinDate(log.getCheckinDate())
                .checkinTime(log.getCheckinTime())
                .qrTokenJti(log.getQrTokenJti())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
