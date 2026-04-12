package com.gymapp.modules.checkin.repository;

import com.gymapp.modules.checkin.entity.Checkin;
import com.gymapp.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CheckinRepository extends JpaRepository<Checkin, UUID> {
    Optional<Checkin> findByUserAndCheckinDate(User user, LocalDate date);
}