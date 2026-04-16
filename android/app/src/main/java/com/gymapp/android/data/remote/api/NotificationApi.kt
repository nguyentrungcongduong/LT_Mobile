package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.api.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

// ── DTOs ─────────────────────────────────────────────────────────────────────

data class UpdateFcmTokenRequest(val fcmToken: String)

data class NotificationDto(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val refId: String?,
    val isRead: Boolean,
    val sentAt: String?,
    val createdAt: String
)

// ── Workout Schedule DTOs ─────────────────────────────────────────────────────

data class WorkoutScheduleDto(
    val id: String,
    val dayOfWeek: String,       // MON TUE WED THU FRI SAT SUN
    val remindTime: String       // "HH:mm:ss"
)

data class SaveWorkoutScheduleRequest(
    val dayOfWeek: String,
    val remindTime: String       // "HH:mm"
)

// ── API Interface ─────────────────────────────────────────────────────────────

interface NotificationApi {

    @GET("api/v1/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<NotificationDto>>>

    @PUT("api/v1/notifications/read-all")
    suspend fun markAllAsRead(): Response<ApiResponse<Void>>

    @PUT("api/v1/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<ApiResponse<Void>>
}

interface WorkoutScheduleApi {

    @GET("api/v1/workout-schedules")
    suspend fun getSchedules(): Response<ApiResponse<List<WorkoutScheduleDto>>>

    @PUT("api/v1/workout-schedules/{day}")
    suspend fun saveSchedule(
        @Path("day") day: String,
        @Body request: SaveWorkoutScheduleRequest
    ): Response<ApiResponse<WorkoutScheduleDto>>

    @PUT("api/v1/workout-schedules/{day}/delete")
    suspend fun deleteSchedule(
        @Path("day") day: String
    ): Response<ApiResponse<Void>>
}
