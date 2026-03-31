package com.gymapp.android.domain.model.membership

import java.time.LocalDate

data class ActiveMembership(
    val id: String,             // UUID - memberships.id
    val planName: String,       // join từ membership_plans.name
    val planType: PlanType,     // ENUM: SINGLE | ALL
    val branchName: String?,    // join từ branches.name, null nếu ALL
    val status: MembershipStatus, // ENUM: PENDING|ACTIVE|EXPIRED|FROZEN|CANCELLED
    val startDate: LocalDate,   // memberships.start_date (DATE)
    val endDate: LocalDate,     // memberships.end_date (DATE)
    val daysLeft: Int           // tính server-side
)
