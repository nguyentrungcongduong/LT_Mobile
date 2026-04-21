package com.gymapp.modules.training.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ExerciseResponse {
    private UUID id;
    private String name;
    private String muscleGroup;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
}