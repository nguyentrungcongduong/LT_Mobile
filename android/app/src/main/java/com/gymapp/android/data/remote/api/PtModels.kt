package com.gymapp.android.data.remote.api

import com.google.gson.annotations.SerializedName
import java.util.Date

data class PtAvailabilityDto(
    val id: String,
    @SerializedName("available_date") val availableDate: String, // YYYY-MM-DD
    @SerializedName("start_time") val startTime: String, // HH:MM
    @SerializedName("end_time") val endTime: String,
    @SerializedName("is_booked") val isBooked: Boolean
)

data class BookingsResponse(
    val availabilities: List<PtAvailabilityDto>
)

data class BookingCreateRequest(
    @SerializedName("pt_id") val ptId: String,
    @SerializedName("availability_id") val availabilityId: String
)

data class BookingCreateResponse(
    @SerializedName("booking_id") val bookingId: String,
    @SerializedName("pt_name") val ptName: String,
    @SerializedName("scheduled_at") val scheduledAt: Date,
    @SerializedName("end_at") val endAt: Date,
    @SerializedName("total_amount") val totalAmount: Double,
    val status: String,
    @SerializedName("payment_url") val paymentUrl: String?,
    @SerializedName("expires_at") val expiresAt: Date?
)

data class CancelBookingRequest(
    val reason: String
)

data class CancelBookingResponse(
    @SerializedName("booking_id") val bookingId: String,
    val status: String,
    @SerializedName("refund_amount") val refundAmount: Double,
    @SerializedName("refund_pct") val refundPct: Int
)

data class BookingDto(
    val id: String,
    @SerializedName("pt_name") val ptName: String?,
    @SerializedName("pt_avatar") val ptAvatar: String?,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("user_avatar") val userAvatar: String?,
    @SerializedName("scheduled_at") val scheduledAt: Date,
    @SerializedName("end_at") val endAt: Date,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("pt_amount") val ptAmount: Double?,
    val status: String
)

data class PageResponse<T>(
    @SerializedName("items") val content: List<T>
)

data class ClientDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("total_sessions") val totalSessions: Long,
    @SerializedName("last_session_at") val lastSessionAt: Date?
)

data class WorkoutLogDto(
    @SerializedName("exercise_name") val exerciseName: String,
    val notes: String?,
    val sets: Int,
    val reps: Int,
    val weight: Double
)

data class ClientSessionDto(
    @SerializedName("booking_id") val bookingId: String,
    val date: Date,
    val status: String,
    @SerializedName("workout_logs") val workoutLogs: List<WorkoutLogDto>
)

data class ClientProgressDto(
    val sessions: List<ClientSessionDto>
)

data class PtPublicDto(
    val id: String,
    val fullName: String,
    val avatarUrl: String?,
    val specializations: List<String>?,
    @SerializedName("ratingAvg") val rating: Double,
    @SerializedName("totalReviews") val reviewCount: Int,
    @SerializedName("pricePerSession") val price: Double
)
