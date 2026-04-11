package com.gymapp.modules.training.repository;

import com.gymapp.modules.training.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
}
