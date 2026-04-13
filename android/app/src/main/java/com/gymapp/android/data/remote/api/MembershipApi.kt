package com.gymapp.android.data.remote.api

import com.gymapp.android.data.remote.dto.membership.ActiveMembershipDto
import com.gymapp.android.data.remote.dto.membership.MembershipPlansResponseDto
import com.gymapp.android.domain.model.workout.WpType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MembershipApi {

    @GET("membership-plans")
    suspend fun getMembershipPlans(
        @Query("branch_id") branchId: String? = null,
        @Query("plan_type") planType: WpType? = null
    ): Response<ApiResponse<MembershipPlansResponseDto>>

    @GET("memberships/me")
    suspend fun getActiveMembership(): Response<ApiResponse<ActiveMembershipDto>>
}
