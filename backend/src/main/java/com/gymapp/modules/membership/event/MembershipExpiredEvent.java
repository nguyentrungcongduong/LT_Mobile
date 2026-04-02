package com.gymapp.modules.membership.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Event được publish khi membership hết hạn
 * 
 * Dùng để trigger các hành động như:
 * - Gửi notification cho user
 * - Log audit
 * - Cập nhật analytics
 */
@Getter
public class MembershipExpiredEvent extends ApplicationEvent {

    private final UUID membershipId;
    private final UUID userId;
    private final String userEmail;
    private final String planName;
    private final LocalDate expiredDate;

    public MembershipExpiredEvent(UUID membershipId, UUID userId, String userEmail, 
                                  String planName, LocalDate expiredDate) {
        super(membershipId);
        this.membershipId = membershipId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.planName = planName;
        this.expiredDate = expiredDate;
    }

    @Override
    public String toString() {
        return String.format("MembershipExpiredEvent{membershipId=%s, userId=%s, planName=%s, expiredDate=%s}",
                membershipId, userId, planName, expiredDate);
    }
}
