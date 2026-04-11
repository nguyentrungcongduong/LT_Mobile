package com.gymapp.modules.training.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "plan_exercises", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "order_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    @Builder.Default
    private Integer sets = 3;

    @Column(nullable = false)
    @Builder.Default
    private Integer reps = 10;

    @Column(name = "rest_seconds", nullable = false)
    @Builder.Default
    private Integer restSeconds = 60;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    private String notes;
}
