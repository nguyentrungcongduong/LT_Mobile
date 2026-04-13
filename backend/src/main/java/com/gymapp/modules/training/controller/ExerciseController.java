package com.gymapp.modules.training.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gymapp.modules.training.dto.ExerciseResponse;
import com.gymapp.modules.training.repository.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseRepository exerciseRepository;

    @GetMapping
    public List<ExerciseResponse> getExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(ex -> new ExerciseResponse(
                        ex.getId(),
                        ex.getName(),
                        ex.getMuscleGroup()))
                .toList();
    }
}
