package com.gymapp.modules.user.service;

import com.gymapp.modules.user.dto.response.CheckinLogResponse;
import com.gymapp.modules.user.entity.Checkin;
import com.gymapp.modules.user.entity.CheckinLog;
import com.gymapp.modules.user.repository.CheckinLogRepository;
import com.gymapp.modules.user.repository.CheckinRepository;
import com.gymapp.modules.membership.entity.Membership;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinLogRepository checkinLogRepository;

    public String checkin(String qrData) {

        // 0. CHECK AUTH
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("Unauthorized");
        }

        String email = auth.getName();

        // 1. Validate QR
        if (!"CHECKIN_GYM".equals(qrData)) {
            throw new IllegalArgumentException("QR không hợp lệ");
        }

        // 2. Lấy user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Validate user
        if (!user.isActive()) {
            throw new IllegalArgumentException("Tài khoản bị khóa");
        }

        if (user.getRole() != UserRole.USER) {
            throw new IllegalArgumentException("Chỉ USER mới được check-in");
        }

        // 4. Membership
        List<Membership> memberships = membershipRepository
                .findActiveMembershipsByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE);

        if (memberships.isEmpty()) {
            throw new IllegalArgumentException("Chưa có gói");
        }

        boolean valid = memberships.stream()
                .anyMatch(m -> !m.getEndDate().isBefore(LocalDate.now()));

        if (!valid) {
            throw new IllegalArgumentException("Hết hạn");
        }

        // 5. Duplicate
        Optional<Checkin> existing = checkinRepository
                .findByUserIdAndCheckinDate(user.getId(), LocalDate.now());

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Đã check-in hôm nay");
        }

        // 6. Save
        Checkin checkin = Checkin.builder()
                .user(user)
                .checkinDate(LocalDate.now())
                .checkinTime(OffsetDateTime.now())
                .build();

        checkinRepository.save(checkin);

        return "Check-in thành công";
    }

    public List<CheckinLogResponse> getMyCheckinHistory(UUID userId) {

        List<CheckinLog> logs = checkinLogRepository.findByUserIdWithAll(userId);

        return logs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CheckinLogResponse mapToResponse(CheckinLog c) {
        return CheckinLogResponse.builder()
                .id(c.getId())
                .checkinTime(c.getCheckinTime())
                .checkinDate(c.getCheckinDate())
                .branchId(c.getBranch() != null ? c.getBranch().getId() : null)
                .branchName(c.getBranch() != null ? c.getBranch().getName() : null)
                .build();
    }

}
