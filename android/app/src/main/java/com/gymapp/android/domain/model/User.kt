package com.gymapp.android.domain.model

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val role: String,
    val avatarUrl: String?
)
