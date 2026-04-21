package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByIsActiveTrue();
    List<User> findByRole(UserRole role);
    List<User> findByIdIn(List<UUID> ids);
}
