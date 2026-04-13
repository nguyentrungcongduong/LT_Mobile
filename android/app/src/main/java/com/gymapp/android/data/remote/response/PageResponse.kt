package com.gymapp.android.data.remote.response

data class PageResponse<T>(
    val content: List<T>?,
    val totalElements: Int?,
    val totalPages: Int?
)