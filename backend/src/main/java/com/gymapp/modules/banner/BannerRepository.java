package com.gymapp.modules.banner;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findByIsActiveTrueOrderByCreatedAtDesc();
}