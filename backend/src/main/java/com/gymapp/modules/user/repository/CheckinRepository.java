package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CheckinRepository extends JpaRepository<Checkin, UUID> {

    @Query("SELECT c FROM Checkin c WHERE c.user.id = :userId AND c.checkinDate = :date")
    Optional<Checkin> findByUserIdAndCheckinDate(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date
    );
}
