package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.PtProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PtProfileRepository extends JpaRepository<PtProfile, UUID> {
    Optional<PtProfile> findByUserId(UUID userId);
}
