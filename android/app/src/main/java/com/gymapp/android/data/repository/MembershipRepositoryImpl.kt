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
        try {
            val response = api.getMembershipPlans(branchId, planType)
            handleResponse(response) { dto ->
                dto.plans.map { it.toDomainModel() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMembershipPlanById(id: String): Result<MembershipPlan?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getMembershipPlanById(id)
            handleResponse(response) { it.toDomainModel() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveMembership(): Result<ActiveMembership> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveMembership()
            handleResponse(response) { it.toDomainModel() }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerMembership(planId: String): Result<ActiveMembership> = withContext(Dispatchers.IO) {
        try {
            val response = api.registerMembership(planId)
            handleResponse(response) { it.toDomainModel() }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
