package com.gymapp.modules.training.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class SaveWorkoutScheduleRequest {
    private String dayOfWeek;
    private String remindTime; // "HH:mm"
}
