package com.gymapp.modules.payment.repository;

import com.gymapp.modules.payment.entity.PtEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PtEarningRepository extends JpaRepository<PtEarning, UUID> {
    Optional<PtEarning> findByBookingId(UUID bookingId);
}
