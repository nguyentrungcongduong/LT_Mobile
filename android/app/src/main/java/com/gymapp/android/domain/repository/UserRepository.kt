package com.gymapp.android.domain.repository

import com.gymapp.android.data.remote.dto.user.UpdateUserGoalRequest
import com.gymapp.android.domain.model.User
import java.io.File

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(fullName: String?, email: String?, phone: String?, avatarUrl: String?): Result<User>
    suspend fun uploadAvatar(file: File): Result<String>
    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit>
    suspend fun updateGoal(request: UpdateUserGoalRequest): Result<Unit>
    suspend fun getAdminDashboardStats(): Result<com.gymapp.android.data.remote.api.AdminDashboardStatsDto>
}
