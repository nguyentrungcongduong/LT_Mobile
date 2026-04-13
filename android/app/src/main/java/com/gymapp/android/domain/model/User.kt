package com.gymapp.android.domain.model

import com.gymapp.android.domain.model.goal.ExperienceLevel
import com.gymapp.android.domain.model.goal.FitnessGoal

data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val role: String,
    val avatarUrl: String?,
    val experienceLevel: ExperienceLevel?,
    val fitnessGoal: FitnessGoal?
)
