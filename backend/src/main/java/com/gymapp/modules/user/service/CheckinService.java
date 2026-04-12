package com.gymapp.modules.checkin.service;

import com.gymapp.modules.checkin.entity.Checkin;
import com.gymapp.modules.checkin.repository.CheckinRepository;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.repository.MembershipRepository;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public String checkin(String qrData) {

        // 1. Validate QR
        if (!"CHECKIN_GYM".equals(qrData)) {
            throw new RuntimeException("QR không hợp lệ");
        }

        // 2. Lấy user từ JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Validate user
        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản bị khóa");
        }

        if (user.getRole() != UserRole.USER) {
            throw new RuntimeException("Chỉ USER mới được check-in");
        }

        // 4. Check membership
        var membership = membershipRepository
                .findFirstByUser_IdAndStatusAndEndDateGreaterThanEqual(
                        user.getId(),
                        MembershipStatus.ACTIVE,
                        LocalDate.now()
                );

        if (membership.isEmpty()) {
            throw new RuntimeException("Bạn chưa có gói tập hoặc đã hết hạn");
        }

        // 5. Save checkin
        Checkin checkin = Checkin.builder()
                .user(user)
                .checkinDate(LocalDate.now())
                .checkinTime(OffsetDateTime.now())
                .build();

        try {
            checkinRepository.save(checkin);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Bạn đã check-in hôm nay");
        }

        return "Check-in thành công";
    }
}