package com.gymapp.android.ui.screens.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.dto.checkin.CheckinLogResponse
import com.gymapp.android.domain.repository.CheckinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository
) : ViewModel() {

    // --- User UI State ---
    private val _qrToken = MutableStateFlow<String?>(null)
    val qrToken: StateFlow<String?> = _qrToken.asStateFlow()

    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val _isLoadingQr = MutableStateFlow(false)
    val isLoadingQr: StateFlow<Boolean> = _isLoadingQr.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var timerJob: Job? = null

    // --- Admin UI State ---
    private val _scanResult = MutableStateFlow<CheckinLogResponse?>(null)
    val scanResult: StateFlow<CheckinLogResponse?> = _scanResult.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    fun fetchQrToken() {
        viewModelScope.launch {
            _isLoadingQr.value = true
            _errorMessage.value = null
            timerJob?.cancel()

            checkinRepository.getQrToken()
                .onSuccess { response ->
                    _qrToken.value = response.qrToken
                    _countdown.value = response.expiresInSeconds.toInt()
                    startTimer()
                }
                .onFailure {
                    _errorMessage.value = it.message ?: "Failed to generate QR token"
                }

            _isLoadingQr.value = false
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000)
                _countdown.value -= 1
            }
            // Auto refresh
            fetchQrToken()
        }
    }

    fun verifyQrToken(token: String, branchId: String? = null) {
        viewModelScope.launch {
            _isVerifying.value = true
            _scanError.value = null
            _scanResult.value = null

            checkinRepository.verifyQrToken(token, branchId)
                .onSuccess { result ->
                    _scanResult.value = result
                }
                .onFailure {
                    _scanError.value = it.message ?: "Failed to verify QR"
                }

            _isVerifying.value = false
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
        _scanError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
