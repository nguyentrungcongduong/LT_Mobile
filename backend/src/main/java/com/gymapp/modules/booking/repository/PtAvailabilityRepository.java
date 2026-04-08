package com.gymapp.modules.booking.repository;

import com.gymapp.modules.booking.entity.PtAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PtAvailabilityRepository extends JpaRepository<PtAvailability, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM PtAvailability a WHERE a.id = :id")
    Optional<PtAvailability> findByIdWithLock(@Param("id") UUID id);

    List<PtAvailability> findAllByPtIdAndAvailableDateBetween(
            UUID ptId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(a) > 0 FROM PtAvailability a " +
            "WHERE a.ptId = :ptId " +
            "AND a.availableDate = :date " +
            "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    boolean existsOverlapping(
            @Param("ptId") UUID ptId,
            @Param("date") LocalDate date,
            @Param("startTime") java.time.LocalTime startTime,
            @Param("endTime") java.time.LocalTime endTime);
}
