package com.gymapp.modules.payment.repository;

import com.gymapp.modules.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByBookingId(UUID bookingId);

    @Query(value = """
        SELECT COALESCE(SUM(p.amount), 0)
        FROM payments p
        WHERE p.status = CAST('SUCCESS' AS payment_status)
                AND p.created_at >= :start
                AND p.created_at < :end
                """, nativeQuery = true)
                BigDecimal sumSuccessfulRevenueBetween(
                        @Param("start") OffsetDateTime start,
                        @Param("end") OffsetDateTime end
        );

    @Query(value = """
        SELECT CAST(p.created_at AS date) AS revenueDate, COALESCE(SUM(p.amount), 0) AS revenue
        FROM payments p
        WHERE p.status = CAST('SUCCESS' AS payment_status)
          AND p.created_at >= :start
          AND p.created_at < :end
        GROUP BY CAST(p.created_at AS date)
        ORDER BY CAST(p.created_at AS date)
        """, nativeQuery = true)
    List<DailyRevenueProjection> findDailyRevenueBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query(value = """
            SELECT pt.full_name AS name, COALESCE(SUM(p.amount), 0) AS revenue
            FROM payments p
            JOIN bookings b ON b.id = p.booking_id
            JOIN users pt ON pt.id = b.pt_id
            WHERE p.status = CAST('SUCCESS' AS payment_status)
              AND p.booking_id IS NOT NULL
            GROUP BY pt.id, pt.full_name
            ORDER BY revenue DESC
            LIMIT 5
            """, nativeQuery = true)
    List<TopPtRevenueProjection> findTopPtRevenue();

    interface DailyRevenueProjection {
        LocalDate getRevenueDate();
        BigDecimal getRevenue();
    }

    interface TopPtRevenueProjection {
        String getName();
        BigDecimal getRevenue();
    }
}
