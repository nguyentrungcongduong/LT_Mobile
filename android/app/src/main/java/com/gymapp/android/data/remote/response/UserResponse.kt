package com.gymapp.android.data.remote.response

data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val role: String,
    val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val weight: Double?,
    val height: Double?,
    val age: Double?,
    val avatarUrl: String?,
    val experienceLevel: String?,
    val fitnessGoal: String?
)