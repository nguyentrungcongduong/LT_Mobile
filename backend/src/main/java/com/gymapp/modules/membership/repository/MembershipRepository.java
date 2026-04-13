package com.gymapp.modules.membership.repository;

import com.gymapp.modules.membership.entity.Membership;
import com.gymapp.modules.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    /**
     * Tìm tất cả membership đang ACTIVE và đã hết hạn (end_date < today)
     * Để scheduled job tự động cập nhật thành EXPIRED
     */
    @Query("SELECT m FROM Membership m WHERE m.status = :status AND m.endDate < :today")
    List<Membership> findExpiredMemberships(
            @Param("status") MembershipStatus status,
            @Param("today") LocalDate today
    );

    /**
     * Tìm membership ACTIVE của user
     */
    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Membership> findActiveMembershipsByUserId(@Param("userId") UUID userId);

    /**
     * New Method checkin
     */
    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.status = :status")
    List<Membership> findActiveMembershipsByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") MembershipStatus status
    );

    /**
     * Tìm membership mới nhất của user (bất kể status)
     */
    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<Membership> findLatestMembershipsByUserId(@Param("userId") UUID userId);

    /**
     * Cập nhật status thành EXPIRED cho nhiều membership
     */
    @Modifying
    @Query("UPDATE Membership m SET m.status = 'EXPIRED' WHERE m.id IN :ids")
    int bulkUpdateStatusToExpired(@Param("ids") List<UUID> ids);

    /**
     * Tìm membership đầu tiên của user với status và endDate phù hợp
     */
    Optional<Membership> findFirstByUser_IdAndStatusAndEndDateGreaterThanEqual(
        @Param("userId") UUID userId,
        @Param("status") MembershipStatus status,
        @Param("today") LocalDate today
    );
}
