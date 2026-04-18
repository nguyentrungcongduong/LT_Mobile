package com.gymapp.modules.membership.dto;

import com.gymapp.modules.membership.enums.ExperienceLevel;
import com.gymapp.modules.membership.enums.FitnessGoal;

import lombok.Data;

@Data
public class UpdateUserGoalRequest {
    private ExperienceLevel experienceLevel;
    private FitnessGoal fitnessGoal;
}
