package com.gymapp.android.data.remote.dto.user

import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal

data class UpdateUserGoalRequest(
    val experienceLevel: ExperienceLevel,
    val fitnessGoal: FitnessGoal
)