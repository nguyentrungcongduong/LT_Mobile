package com.gymapp.android.ui.screens.membership.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.domain.model.membership.MembershipPlan
import com.gymapp.android.domain.usecase.membership.GetMembershipPlanByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gymapp.android.domain.usecase.membership.GetActiveMembershipUseCase
import java.time.format.DateTimeFormatter
import com.gymapp.android.domain.repository.PaymentRepository
import com.gymapp.android.domain.repository.MembershipRepository
import com.gymapp.android.data.remote.api.PaymentInitiateRequestDto

data class PackageDetailUiState(
    val isLoading: Boolean = true,
    val plan: MembershipPlan? = null,
    val error: String? = null,
    
    // Thêm các thuộc tính để check trạng thái gói hiện tại của user
    val isCurrentUserPlan: Boolean = false,
    val isExpired: Boolean = false,
    val activeUntil: String? = null,
    val gatewayUrl: String? = null // Thêm cho payment
)


@HiltViewModel
class PackageDetailViewModel @Inject constructor(
    private val getMembershipPlanByIdUseCase: GetMembershipPlanByIdUseCase,
    private val getActiveMembershipUseCase: GetActiveMembershipUseCase,
    private val membershipRepository: MembershipRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackageDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("planId")?.let { id ->
            loadPlan(id)
        } ?: run {
            _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy ID gói tập") }
        }
    }

    fun initiatePayment(provider: String, planId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Tạo gói tập trạng thái PENDING
            val registerResult = membershipRepository.registerMembership(planId)
            
            if (registerResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "Không thể đăng ký gói tập") }
                return@launch
            }
            
            val membership = registerResult.getOrNull()
            
            if (membership != null) {
                // 2. Tạo URL thanh toán VNPAY/MOMO
                val request = PaymentInitiateRequestDto(
                    membershipId = membership.id,
                    provider = provider
                )
                
                val paymentResult = paymentRepository.initiatePayment(request)
                
                if (paymentResult.isSuccess) {
                    val gatewayUrl = paymentResult.getOrNull()?.gatewayUrl
                    _uiState.update { it.copy(isLoading = false, gatewayUrl = gatewayUrl) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không thể tạo giao dịch thanh toán") }
                }
            }
        }
    }

    fun clearGatewayUrl() {
        _uiState.update { it.copy(gatewayUrl = null) }
    }

    private fun loadPlan(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getMembershipPlanByIdUseCase(id)
            if (result.isSuccess) {
                val plan = result.getOrNull()
                if (plan != null) {
                    val activeMembershipResult = getActiveMembershipUseCase()
                    val activeMembership = activeMembershipResult.getOrNull()
                    
                    var isCurrentUserPlan = false
                    var isExpired = false
                    var activeUntil: String? = null

                    if (activeMembership != null && activeMembership.planName == plan.name) {
                        isCurrentUserPlan = true
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        activeUntil = activeMembership.endDate.format(formatter)
                        isExpired = activeMembership.daysLeft <= 0 || activeMembership.status != com.gymapp.android.domain.model.membership.MembershipStatus.ACTIVE
                    }

                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            plan = plan,
                            isCurrentUserPlan = isCurrentUserPlan,
                            activeUntil = activeUntil,
                            isExpired = isExpired
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Gói tập không tồn tại") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải dữ liệu") }
            }
        }
    }
}
