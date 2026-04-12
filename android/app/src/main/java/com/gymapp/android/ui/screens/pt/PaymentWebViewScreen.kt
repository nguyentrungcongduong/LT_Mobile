package com.gymapp.android.ui.screens.pt

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentWebViewScreen(
    encodedUrl: String,
    onNavigateBack: () -> Unit,
    onPaymentResultReceived: (String) -> Unit // booking_id
) {
    val url = remember { 
        String(android.util.Base64.decode(encodedUrl, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
    }
    var isLoading by remember { mutableStateOf(true) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Hủy thanh toán?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Lịch hẹn còn hiệu lực thêm khoảng 15 phút.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { 
                    showCancelDialog = false
                    onNavigateBack() // Go back to Confirm Screen without finalizing
                }) {
                    Text("Hủy", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Tiếp tục thanh toán", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cổng thanh toán", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Trở về", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            }
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val loadingUrl = request?.url.toString()
                                if (loadingUrl.startsWith("gymapp://payment/result")) {
                                    val uri = android.net.Uri.parse(loadingUrl)
                                    val bookingId = uri.getQueryParameter("booking_id") ?: ""
                                    onPaymentResultReceived(bookingId)
                                    return true
                                }
                                
                                // Bắt tất cả các scheme không phải HTTP/HTTPS (ví dụ: momo://, vnpay://, intent://)
                                if (!loadingUrl.startsWith("http://") && !loadingUrl.startsWith("https://")) {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(loadingUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Không thể mở ứng dụng ngoài", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    return true
                                }

                                return super.shouldOverrideUrlLoading(view, request)
                            }
                        }
                        loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
