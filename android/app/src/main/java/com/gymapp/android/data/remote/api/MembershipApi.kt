package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.dto.membership.ActiveMembershipDto
import com.gymapp.android.data.remote.dto.membership.MembershipPlanDto
import com.gymapp.android.data.remote.dto.membership.MembershipPlansResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MembershipApi {

    @GET("api/v1/membership-plans")
    suspend fun getMembershipPlans(
        @Query("branch_id") branchId: String? = null,
        @Query("plan_type") planType: String? = null
    ): Response<ApiResponse<MembershipPlansResponseDto>>

    @GET("api/v1/membership-plans/{id}")
    suspend fun getMembershipPlanById(@Path("id") id: String): Response<ApiResponse<MembershipPlanDto>>

    @GET("api/v1/memberships/me")
    suspend fun getActiveMembership(): Response<ApiResponse<ActiveMembershipDto>>

    @POST("api/v1/memberships/register/{planId}")
    suspend fun registerMembership(@Path("planId") planId: String): Response<ApiResponse<ActiveMembershipDto>>
}
