package com.gymapp.android.ui.screens.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymapp.android.data.remote.response.BannerResponse
import com.gymapp.android.data.repository.BannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BannerViewModel @Inject constructor(
    private val bannerRepository: BannerRepository
) : ViewModel() {

    private val _banners = MutableStateFlow<List<BannerResponse>>(emptyList())
    val banners: StateFlow<List<BannerResponse>> = _banners

    init {
        fetchBanners()
    }

    private fun fetchBanners() {
        viewModelScope.launch {
            bannerRepository.getActiveBanners()
                .onSuccess { _banners.value = it }
                .onFailure { _banners.value = emptyList() }
        }
    }
}