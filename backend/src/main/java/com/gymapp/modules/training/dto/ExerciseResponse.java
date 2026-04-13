package com.gymapp.modules.training.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExerciseResponse {
    private UUID id;
    private String name;
    private String muscleGroup;
}