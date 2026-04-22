package com.gymapp.android.data.remote.dto.checkin

data class QrTokenResponse(
    val qrToken: String,
    val expiresInSeconds: Long
)

data class CheckinVerifyRequest(
    val qrToken: String,
    val branchId: String? = null
)

data class CheckinLogResponse(
    val id: String,
    val userId: String,
    val userEmail: String,
    val userFullName: String,
    val branchId: String?,
    val checkinDate: String,
    val checkinTime: String,
    val qrTokenJti: String,
    val createdAt: String,
    val branchName: String?
)

data class CheckinStatsResponse(
    val totalSessions: Long,
    val streakDays: Int,
    val totalHours: Double
)

