package com.gymapp.modules.training.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveWorkoutScheduleRequest {
    private String dayOfWeek;
    private String remindTime; // "HH:mm"
}
