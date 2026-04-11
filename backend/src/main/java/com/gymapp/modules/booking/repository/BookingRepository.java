package com.gymapp.modules.booking.repository;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findAllByUserIdAndStatus(UUID userId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByPtIdAndStatus(UUID ptId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, OffsetDateTime now);

    List<Booking> findAllByStatusAndEndAtBefore(BookingStatus status, OffsetDateTime now);

    @org.springframework.data.jpa.repository.Query("SELECT new com.gymapp.modules.booking.dto.PtClientSummary(b.user.id, b.user.fullName, b.user.avatarUrl, COUNT(b.id), MAX(b.scheduledAt)) "
            +
            "FROM Booking b WHERE b.pt.id = :ptId GROUP BY b.user.id, b.user.fullName, b.user.avatarUrl")
    Page<com.gymapp.modules.booking.dto.PtClientSummary> findClientSummariesByPtId(UUID ptId, Pageable pageable);

    List<Booking> findAllByUserIdAndPtIdOrderByScheduledAtDesc(UUID userId, UUID ptId);

    @Query("""
                SELECT b FROM Booking b
                LEFT JOIN FETCH b.pt
                LEFT JOIN FETCH b.user
            """)
    List<Booking> findAllWithRelations();
}
