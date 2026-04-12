package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.PaymentApi
import com.gymapp.android.data.remote.api.PaymentHistoryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PaymentHistoryUiState {
    object Loading : PaymentHistoryUiState()
    data class Success(
        val items: List<PaymentHistoryDto>,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = false
    ) : PaymentHistoryUiState()
    data class Error(val message: String) : PaymentHistoryUiState()
}

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val paymentApi: PaymentApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentHistoryUiState>(PaymentHistoryUiState.Loading)
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var isLastPage = false
    private var isFetching = false

    init {
        loadHistory()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        resetAndLoad()
    }

    fun loadHistory() {
        resetAndLoad()
    }

    private fun resetAndLoad() {
        currentPage = 0
        isLastPage = false
        isFetching = false
        fetchPage(reset = true)
    }

    fun loadNextPage() {
        if (isLastPage || isFetching) return
        fetchPage(reset = false)
    }

    private fun fetchPage(reset: Boolean) {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            if (reset) {
                _uiState.value = PaymentHistoryUiState.Loading
            } else {
                val current = _uiState.value
                if (current is PaymentHistoryUiState.Success) {
                    _uiState.value = current.copy(isLoadingMore = true)
                }
            }

            try {
                // Tab 1: Lịch PT — chỉ lấy paymentType=BOOKING và status=SUCCESS
                // Tab 2: Hội viên  — tất cả paymentType=MEMBERSHIP, kể cả REFUNDED
                // Tab 3: Hoàn tiền — chỉ status=REFUNDED, mọi loại
                val (type, status) = when (_selectedTab.value) {
                    1 -> Pair("BOOKING", "SUCCESS")
                    2 -> Pair("MEMBERSHIP", null)
                    3 -> Pair(null, "REFUNDED")
                    else -> Pair(null, null)
                }

                val response = paymentApi.getPaymentHistory(
                    paymentType = type,
                    status = status,
                    page = currentPage,
                    size = pageSize
                )

                if (response.isSuccessful) {
                    val page = response.body()?.data
                    val newItems = page?.content ?: emptyList()
                    val totalPages = page?.totalPages ?: 1
                    isLastPage = currentPage >= totalPages - 1

                    val existingItems = if (reset) emptyList()
                    else (_uiState.value as? PaymentHistoryUiState.Success)?.items ?: emptyList()

                    _uiState.value = PaymentHistoryUiState.Success(
                        items = existingItems + newItems,
                        isLoadingMore = false,
                        hasMore = !isLastPage
                    )
                    currentPage++
                } else {
                    if (reset) {
                        _uiState.value = PaymentHistoryUiState.Error("Không tải được lịch sử giao dịch")
                    } else {
                        val current = _uiState.value
                        if (current is PaymentHistoryUiState.Success) {
                            _uiState.value = current.copy(isLoadingMore = false)
                        }
                    }
                }
            } catch (e: Exception) {
                if (reset) {
                    _uiState.value = PaymentHistoryUiState.Error(e.message ?: "Lỗi kết nối")
                } else {
                    val current = _uiState.value
                    if (current is PaymentHistoryUiState.Success) {
                        _uiState.value = current.copy(isLoadingMore = false)
                    }
                }
            } finally {
                isFetching = false
            }
        }
    }
}
