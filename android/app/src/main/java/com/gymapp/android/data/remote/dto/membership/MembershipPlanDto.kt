package com.gymapp.android.data.remote.dto.membership

import com.google.gson.annotations.SerializedName
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.model.membership.PlanType

data class MembershipPlansResponseDto(
    val plans: List<MembershipPlanDto>
)

data class BranchResponseDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("is_active") val isActive: Boolean
)

data class MembershipPlanDto(
    val id: String,                                   // UUID
    val name: String,
    val description: String?,                         // nullable trong DB
    val price: Double,                                // NUMERIC(10,2)
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("plan_type") val planType: String, // "SINGLE" | "ALL"
    @SerializedName("branch_id") val branchId: String?, // UUID, null nếu ALL
    @SerializedName("branch_name") val branchName: String?,
    val branchLatitude: Double?,
    val branchLongitude: Double?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("available_branches") val availableBranches: List<BranchResponseDto>? = null
) {
    fun toDomainModel(): MembershipPlan {
        return MembershipPlan(
            id = id,
            name = name,
            description = description ?: "",
            price = price,
            durationDays = durationDays,
            planType = try { PlanType.valueOf(planType) } catch (e: Exception) { PlanType.SINGLE },
            branchId = branchId,
            branchName = branchName,
            branchLatitude = branchLatitude,
            branchLongitude = branchLongitude,
            isActive = isActive,
            availableBranches = availableBranches?.map { 
                com.gymapp.android.domain.model.membership.BranchLocation(
                    id = it.id,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            } ?: emptyList()
        )
    }
}
