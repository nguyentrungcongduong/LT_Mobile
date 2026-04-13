package com.gymapp.android.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gymapp.android.data.remote.api.ApiResponse
import com.gymapp.android.data.remote.api.MembershipApi
import com.gymapp.android.domain.model.membership.ActiveMembership
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.repository.MembershipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class MembershipRepositoryImpl @Inject constructor(
    private val api: MembershipApi
) : MembershipRepository{

    private val gson = Gson()

    override suspend fun getMembershipPlans(
        branchId: String?,
        planType: String?
    ): Result<List<MembershipPlan>> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1000) // Giả lập mạng chậm 1s
        
        val allPlans = listOf(
            MembershipPlan(
                id = "a1b2c3d4-0001-0000-0000-000000000001", // UUID format
                name = "Cơ bản",
                description = "Tập luyện không giới hạn tại 1 chi nhánh cố định. Tiết kiệm nhất cho người mới.",
                price = 299000.0,          // NUMERIC(10,2)
                durationDays = 30,
                planType = com.gymapp.android.domain.model.membership.PlanType.SINGLE,
                branchId = "b0000000-0000-0000-0000-000000000001", // UUID
                branchName = "Chi nhánh Q1",
                branchLatitude = 10.776354032266614,
                branchLongitude = 106.69346708476218,
                isActive = true
            ),
            MembershipPlan(
                id = "a1b2c3d4-0001-0000-0000-000000000002",
                name = "Premium",
                description = "Tập luyện không giới hạn tại toàn bộ hệ thống chi nhánh của chúng tôi.",
                price = 749000.0,          // NUMERIC(10,2)
                durationDays = 90,
                planType = com.gymapp.android.domain.model.membership.PlanType.ALL,
                branchId = null,           // NULL khi plan_type = ALL (theo DB)
                branchName = null,         // NULL khi plan_type = ALL
                branchLatitude = null,
                branchLongitude = null,
                isActive = true
            ),
            MembershipPlan(
                id = "a1b2c3d4-0001-0000-0000-000000000003",
                name = "Elite",
                description = "Gói 1 năm siêu tiết kiệm. Tặng kèm 2 buổi PT miễn phí.",
                price = 2400000.0,         // NUMERIC(10,2)
                durationDays = 365,
                planType = com.gymapp.android.domain.model.membership.PlanType.ALL,
                branchId = null,
                branchName = null,
                branchLatitude = null,
                branchLongitude = null,
                isActive = true
            )
        )
        
        Result.success(allPlans.filter { if (planType != null) it.planType.name == planType else true })
    }

    override suspend fun getMembershipPlanById(id: String): Result<MembershipPlan?> = withContext(Dispatchers.IO) {
        val plans = getMembershipPlans(null, null).getOrNull() ?: emptyList()
        Result.success(plans.find { it.id == id })
    }

    override suspend fun getActiveMembership(): Result<ActiveMembership> = withContext(Dispatchers.IO) {
        kotlinx.coroutines.delay(1000) // Giả lập mạng chậm 1s
        
        // Return EmptyState (chưa có gói)
        return@withContext Result.failure(Exception("NO_MEMBERSHIP_FOUND"))
        
//        Result.success(
//            ActiveMembership(
//                id = "c0000000-0000-0000-0000-000000000001", // UUID - memberships.id
//                planName = "Premium",                        // plan_name join từ membership_plans
//                planType = com.gymapp.android.domain.model.membership.PlanType.ALL,
//                branchName = null,  // null khi plan_type = ALL (theo DB)
//                status = com.gymapp.android.domain.model.membership.MembershipStatus.ACTIVE,
//                startDate = java.time.LocalDate.now().minusDays(18), // DATE
//                endDate = java.time.LocalDate.now().plusDays(72),    // DATE
//                daysLeft = 72  // tính server-side
//            )
//        )
    }

    private fun <T, R> handleResponse(
        response: Response<ApiResponse<T>>,
        mapper: (T) -> R
    ): Result<R> {
        return if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()?.data
            if (data != null) {
                Result.success(mapper(data))
            } else {
                Result.failure(Exception("Dữ liệu trống"))
            }
        } else {
            val errorMsg = parseErrorMessage(response)
            Result.failure(Exception(errorMsg))
        }
    }

    private fun <T> parseErrorMessage(response: Response<ApiResponse<T>>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                val type = object : TypeToken<ApiResponse<Any>>() {}.type
                val errorResponse: ApiResponse<Any> = gson.fromJson(errorBody, type)
                errorResponse.message ?: "Đã có lỗi xảy ra"
            } else {
                response.body()?.message ?: "Đã có lỗi xảy ra"
            }
        } catch (e: Exception) {
            "Lỗi kết nối server"
        }
    }
}
