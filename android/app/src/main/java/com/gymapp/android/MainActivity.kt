package com.gymapp.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.gymapp.android.data.local.TokenStorage
import com.gymapp.android.service.FcmTokenUploader
import com.gymapp.android.ui.navigation.AppNavigation
import com.gymapp.android.ui.theme.GymAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStorage: TokenStorage
    @Inject lateinit var fcmTokenUploader: FcmTokenUploader

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — handled silently */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Upload FCM token nếu user đã login
        if (tokenStorage.getAccessToken() != null) {
            try {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token: String ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            fcmTokenUploader.upload(token)
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Firebase init skipped: ${e.message}")
            }
        }

        setContent {
            GymAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
