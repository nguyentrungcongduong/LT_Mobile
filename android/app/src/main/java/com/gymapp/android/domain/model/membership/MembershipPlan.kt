package com.gymapp.android.domain.model.membership

data class MembershipPlan(
    val id: String,           // UUID
    val name: String,
    val description: String,  // nullable trong DB, DTO map sang ""
    val price: Double,        // NUMERIC(10,2) trong DB
    val durationDays: Int,
    val planType: PlanType,   // ENUM: SINGLE | ALL
    val branchId: String?,    // UUID, null nếu plan_type = ALL
    val branchName: String?,  // join từ branches, null nếu ALL
    val isActive: Boolean
)
