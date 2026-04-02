package com.gymapp.modules.membership.scheduler;

import com.gymapp.modules.membership.entity.Membership;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.event.MembershipExpiredEvent;
import com.gymapp.modules.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled job chạy hàng ngày để kiểm tra và cập nhật membership hết hạn
 * 
 * Chạy lúc 00:00 mỗi ngày để scan các membership ACTIVE có end_date < today
 * và tự động chuyển sang status EXPIRED
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final MembershipRepository membershipRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Chạy mỗi ngày lúc 00:00
     * Cron: giây phút giờ ngày tháng thứ (0 0 0 * * ?)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkAndExpireMemberships() {
        log.info("Starting daily membership expiry check...");
        
        LocalDate today = LocalDate.now();
        
        // Tìm tất cả membership đang ACTIVE nhưng đã hết hạn
        List<Membership> expiredMemberships = membershipRepository.findExpiredMemberships(
                MembershipStatus.ACTIVE, 
                today
        );
        
        if (expiredMemberships.isEmpty()) {
            log.info("No expired memberships found.");
            return;
        }
        
        log.info("Found {} expired memberships to process", expiredMemberships.size());
        
        // Cập nhật status thành EXPIRED
        List<java.util.UUID> expiredIds = expiredMemberships.stream()
                .map(Membership::getId)
                .collect(Collectors.toList());
        
        int updatedCount = membershipRepository.bulkUpdateStatusToExpired(expiredIds);
        log.info("Updated {} memberships to EXPIRED status", updatedCount);
        
        // Publish event để gửi notification
        for (Membership membership : expiredMemberships) {
            MembershipExpiredEvent event = new MembershipExpiredEvent(
                    membership.getId(),
                    membership.getUser().getId(),
                    membership.getUser().getEmail(),
                    membership.getPlan().getName(),
                    membership.getEndDate()
            );
            eventPublisher.publishEvent(event);
            log.info("Published MembershipExpiredEvent for membershipId={}, userId={}",
                    membership.getId(), membership.getUser().getId());
        }
        
        log.info("Daily membership expiry check completed.");
    }
}
