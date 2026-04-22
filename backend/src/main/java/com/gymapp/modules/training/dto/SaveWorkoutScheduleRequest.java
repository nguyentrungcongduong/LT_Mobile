package com.gymapp.modules.training.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveWorkoutScheduleRequest {
    private String dayOfWeek;
    private String remindTime; // "HH:mm"
}
