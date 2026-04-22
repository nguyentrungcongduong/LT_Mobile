package com.gymapp.modules.membership.dto;

import com.gymapp.modules.membership.enums.ExperienceLevel;
import com.gymapp.modules.membership.enums.FitnessGoal;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserGoalRequest {
    private ExperienceLevel experienceLevel;
    private FitnessGoal fitnessGoal;
}
