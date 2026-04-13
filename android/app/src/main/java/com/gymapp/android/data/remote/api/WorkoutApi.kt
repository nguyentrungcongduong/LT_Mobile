package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.dto.ExerciseResponse
import com.gymapp.android.data.remote.dto.WorkoutLogRequest
import com.gymapp.android.data.remote.dto.WorkoutLogResponse
import com.gymapp.android.data.remote.dto.WorkoutPlanRequest
import com.gymapp.android.data.remote.response.PageResponse  //
import com.gymapp.android.data.remote.response.WorkoutPlanResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutApi {

    @GET("api/v1/workout-plans")
    suspend fun getPlans(
        @Query("type") type: String
    ): PageResponse<WorkoutPlanResponse>

    @GET("api/v1/workout-plans/{id}")
    suspend fun getPlanById(
        @Path("id") id: String
    ): WorkoutPlanResponse

    @GET("api/v1/workout-plans/recommended")
    suspend fun getRecommended(): PageResponse<WorkoutPlanResponse>

    @POST("api/v1/workout-logs")
    suspend fun createWorkoutLog(
        @Body request: WorkoutLogRequest
    ): WorkoutLogResponse

    @POST("api/v1/workout-plans")
    suspend fun createWorkoutPlan(
        @Body request: WorkoutPlanRequest
    ): WorkoutPlanResponse

    @GET("api/v1/exercises")
    suspend fun getExercises(): List<ExerciseResponse>

    @GET("api/v1/workout-logs")
    suspend fun getWorkoutLogs(): PageResponse<WorkoutLogResponse>
}