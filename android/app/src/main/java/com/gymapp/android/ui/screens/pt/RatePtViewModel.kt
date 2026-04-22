package com.gymapp.android.ui.screens.pt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.api.PtApi
import com.gymapp.android.data.remote.api.SubmitReviewRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RatePtUiState {
    object Initial : RatePtUiState()
    object Loading : RatePtUiState()
    data class Success(val message: String = "Đánh giá thành công!") : RatePtUiState()
    data class Error(val message: String) : RatePtUiState()
}

@HiltViewModel
class RatePtViewModel @Inject constructor(
    private val ptApi: PtApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<RatePtUiState>(RatePtUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun submitReview(ptId: String, request: SubmitReviewRequest) {
        viewModelScope.launch {
            _uiState.value = RatePtUiState.Loading
            try {
                val response = ptApi.submitReview(ptId, request)
                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        _uiState.value = RatePtUiState.Success()
                    }
                    response.code() == 409 -> {
                        _uiState.value = RatePtUiState.Error("Bạn đã đánh giá PT này rồi.")
                    }
                    response.code() == 404 -> {
                        _uiState.value = RatePtUiState.Error("Không tìm thấy PT.")
                    }
                    response.code() == 401 -> {
                        _uiState.value = RatePtUiState.Error("Vui lòng đăng nhập lại để đánh giá.")
                    }
                    else -> {
                        // Try to parse error body
                        val errBody = response.errorBody()?.string()
                        val msg = if (!errBody.isNullOrBlank() && errBody.contains("message")) {
                            try {
                                org.json.JSONObject(errBody).optString("message", "")
                                    .ifBlank { "Đánh giá thất bại (${response.code()}). Vui lòng thử lại." }
                            } catch (e: Exception) {
                                "Đánh giá thất bại (${response.code()}). Vui lòng thử lại."
                            }
                        } else {
                            "Đánh giá thất bại (${response.code()}). Vui lòng thử lại."
                        }
                        _uiState.value = RatePtUiState.Error(msg)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = RatePtUiState.Error("Lỗi kết nối: ${e.localizedMessage}")
            }
        }
    }

    fun reset() {
        _uiState.value = RatePtUiState.Initial
    }
}
