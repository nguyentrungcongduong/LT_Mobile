package com.gymapp.modules.booking.repository;

import com.gymapp.modules.booking.entity.Booking;
import com.gymapp.modules.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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

        List<Booking> findAllByUserIdAndPtIdOrderByScheduledAtDesc(UUID userId, UUID ptId);

        @Query("""
                        <<<<<<< HEAD

                                                    SELECT b FROM Booking b
                                                    LEFT JOIN FETCH b.pt
                                                    LEFT JOIN FETCH b.user
                                                """)

        List<Booking> findAllWithRelations();

        Optional<Booking> findFirstByAvailabilityIdAndStatusIn(UUID availabilityId, List<BookingStatus> statuses);

        @Query(value = """
                            SELECT b.* FROM bookings b
                            JOIN users p ON p.id = b.pt_id
                            JOIN users u ON u.id = b.user_id
                            WHERE (CAST(:status AS text) IS NULL OR b.status = CAST(:status AS booking_status))
                            AND (CAST(:ptName AS text) IS NULL OR p.full_name ILIKE CONCAT('%', CAST(:ptName AS text), '%'))
                            AND (CAST(:search AS text) IS NULL OR
                                 u.full_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR
                                 p.full_name ILIKE CONCAT('%', CAST(:search AS text), '%'))
                            AND (CAST(:fromDate AS timestamptz) IS NULL OR b.scheduled_at >= CAST(:fromDate AS timestamptz))
                            AND (CAST(:toDate AS timestamptz) IS NULL OR b.scheduled_at <= CAST(:toDate AS timestamptz))
                        """, countQuery = """
                            SELECT COUNT(*) FROM bookings b
                            JOIN users p ON p.id = b.pt_id
                            JOIN users u ON u.id = b.user_id
                            WHERE (CAST(:status AS text) IS NULL OR b.status = CAST(:status AS booking_status))
                            AND (CAST(:ptName AS text) IS NULL OR p.full_name ILIKE CONCAT('%', CAST(:ptName AS text), '%'))
                            AND (CAST(:search AS text) IS NULL OR
                                 u.full_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR
                                 p.full_name ILIKE CONCAT('%', CAST(:search AS text), '%'))
                            AND (CAST(:fromDate AS timestamptz) IS NULL OR b.scheduled_at >= CAST(:fromDate AS timestamptz))
                            AND (CAST(:toDate AS timestamptz) IS NULL OR b.scheduled_at <= CAST(:toDate AS timestamptz))
                        """, nativeQuery = true)
        Page<Booking> findAllWithFilters(
                        @Param("status") String status,
                        @Param("ptName") String ptName,
                        @Param("search") String search,
                        @Param("fromDate") OffsetDateTime fromDate,
                        @Param("toDate") OffsetDateTime toDate,
                        Pageable pageable);
}
