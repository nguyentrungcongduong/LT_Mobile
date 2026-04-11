package com.gymapp.modules.booking.repository;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

        Page<Booking> findAllByUserIdAndStatus(UUID userId, BookingStatus status, Pageable pageable);

        Page<Booking> findAllByPtIdAndStatus(UUID ptId, BookingStatus status, Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b JOIN b.availability a " +
                "WHERE b.pt.id = :ptId " +
                "AND a.availableDate >= :today " +
                "AND b.status NOT IN :excludeStatuses")
        Page<Booking> findUpcomingAll(
                @org.springframework.data.repository.query.Param("ptId") UUID ptId,
                @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
                @org.springframework.data.repository.query.Param("excludeStatuses") List<BookingStatus> excludeStatuses,
                Pageable pageable);

        @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b JOIN b.availability a " +
                "WHERE b.pt.id = :ptId " +
                "AND b.status = :status " +
                "AND a.availableDate >= :today")
        Page<Booking> findUpcomingByStatus(
                @org.springframework.data.repository.query.Param("ptId") UUID ptId,
                @org.springframework.data.repository.query.Param("status") BookingStatus status,
                @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
                Pageable pageable);

        List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, OffsetDateTime now);

        List<Booking> findAllByStatusAndEndAtBefore(BookingStatus status, OffsetDateTime now);

        @org.springframework.data.jpa.repository.Query("SELECT new com.gymapp.modules.booking.dto.PtClientSummary(b.user.id, b.user.fullName, b.user.avatarUrl, COUNT(b.id), MAX(b.scheduledAt)) "
                        +
                        "FROM Booking b WHERE b.pt.id = :ptId AND b.status IN :statuses GROUP BY b.user.id, b.user.fullName, b.user.avatarUrl")
        Page<com.gymapp.modules.booking.dto.PtClientSummary> findClientSummariesByPtId(
                        @org.springframework.data.repository.query.Param("ptId") UUID ptId,
                        @org.springframework.data.repository.query.Param("statuses") List<BookingStatus> statuses,
                        Pageable pageable);

        List<Booking> findAllByUserIdAndPtIdAndStatusInOrderByScheduledAtDesc(UUID userId, UUID ptId,
                        List<BookingStatus> statuses);
}
