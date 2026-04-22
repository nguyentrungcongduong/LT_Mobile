package com.gymapp.android.data.remote.response

data class BannerResponse(
    val id: String,
    val imageUrl: String,
    val title: String? = null,
    val description: String? = null,
    val isActive: Boolean = true
)