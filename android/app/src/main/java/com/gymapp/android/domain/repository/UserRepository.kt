package com.gymapp.android.domain.repository

import com.gymapp.android.domain.model.User
import java.io.File

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(fullName: String?, phone: String?, avatarUrl: String?): Result<User>
    suspend fun uploadAvatar(file: File): Result<String>
}
