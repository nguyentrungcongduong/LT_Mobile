package com.gymapp.android.data.remote.dto.membership

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
    // Backend BranchResponse uses @JsonProperty("isActive") explicitly
    val isActive: Boolean = true
)

data class MembershipPlanDto(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,

    // Backend sends camelCase (no global snake_case config on server)
    val durationDays: Int = 0,           // "durationDays" from backend
    val planType: String = "SINGLE",     // "planType" from backend
    val branchId: String? = null,        // "branchId" from backend
    val branchName: String? = null,      // "branchName" from backend
    val branchLatitude: Double? = null,  // "branchLatitude" from backend
    val branchLongitude: Double? = null, // "branchLongitude" from backend

    // Lombok primitive boolean → Jackson serializes as "active" (not "isActive")
    val active: Boolean = true,

    val availableBranches: List<BranchResponseDto>? = null
) {
    fun toDomainModel(): MembershipPlan {
        return MembershipPlan(
            id          = id,
            name        = name,
            description = description ?: "",
            price       = price,
            durationDays = durationDays,
            planType    = try { PlanType.valueOf(planType) } catch (e: Exception) { PlanType.SINGLE },
            branchId    = branchId,
            branchName  = branchName,
            branchLatitude  = branchLatitude,
            branchLongitude = branchLongitude,
            isActive    = active,
            availableBranches = availableBranches?.map {
                com.gymapp.android.domain.model.membership.BranchLocation(
                    id        = it.id,
                    name      = it.name,
                    latitude  = it.latitude,
                    longitude = it.longitude
                )
            } ?: emptyList()
        )
    }
}
