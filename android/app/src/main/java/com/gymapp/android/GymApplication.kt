package com.gymapp.android

import android.app.Application
import com.gymapp.android.service.GymFcmService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GymApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tạo notification channels ngay khi app khởi động
        GymFcmService.createNotificationChannels(this)
    }
}
