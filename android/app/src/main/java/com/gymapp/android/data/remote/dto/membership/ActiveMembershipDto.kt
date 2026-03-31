package com.gymapp.android.data.remote.dto.membership

import com.google.gson.annotations.SerializedName
import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.MembershipStatus
import com.gymapp.android.domain.model.membership.PlanType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ActiveMembershipDto(
    val id: String,
    @SerializedName("plan_name") val planName: String,
    @SerializedName("plan_type") val planType: String,
    @SerializedName("branch_name") val branchName: String?,
    val status: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("days_left") val daysLeft: Int
) {
    fun toDomainModel(): ActiveMembership {
        return ActiveMembership(
            id = id,
            planName = planName,
            planType = try { PlanType.valueOf(planType) } catch (e: Exception) { PlanType.SINGLE },
            branchName = branchName,
            status = try { MembershipStatus.valueOf(status) } catch (e: Exception) { MembershipStatus.PENDING },
            startDate = try { LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE) } catch (e: Exception) { LocalDate.now() },
            endDate = try { LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE) } catch (e: Exception) { LocalDate.now() },
            daysLeft = daysLeft
        )
    }
}
