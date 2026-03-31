package com.gymapp.android.data.remote.dto.membership

import com.google.gson.annotations.SerializedName
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.model.membership.PlanType

data class MembershipPlansResponseDto(
    val plans: List<MembershipPlanDto>
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
    @SerializedName("is_active") val isActive: Boolean
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
            isActive = isActive
        )
    }
}
