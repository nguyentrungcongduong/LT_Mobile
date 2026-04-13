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
                    onNavigateBack()
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
                title = {
                    Text(
                        "Cổng thanh toán",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Trở về",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
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
                            // Bắt buộc để WebView tự xử lý popup (window.open)
                            // thay vì chuyển sang Chrome bên ngoài
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val loadingUrl = request?.url.toString()

                                // Bắt deep link callback từ app
                                if (loadingUrl.startsWith("gymapp://payment/result")) {
                                    val uri = android.net.Uri.parse(loadingUrl)
                                    val bookingId = uri.getQueryParameter("booking_id") ?: ""
                                    onPaymentResultReceived(bookingId)
                                    return true
                                }

                                // Bắt các scheme không phải HTTP/HTTPS (vnpay://, momo://, intent://…)
                                if (!loadingUrl.startsWith("http://") && !loadingUrl.startsWith("https://")) {
                                    try {
                                        if (loadingUrl.startsWith("intent://")) {
                                            val intent = android.content.Intent.parseUri(
                                                loadingUrl,
                                                android.content.Intent.URI_INTENT_SCHEME
                                            )
                                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                            when {
                                                intent.resolveActivity(context.packageManager) != null -> {
                                                    context.startActivity(intent)
                                                }
                                                fallbackUrl != null -> {
                                                    view?.loadUrl(fallbackUrl)
                                                }
                                                intent.`package` != null -> {
                                                    context.startActivity(
                                                        android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            android.net.Uri.parse("market://details?id=${intent.`package`}")
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            context.startActivity(
                                                android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse(loadingUrl)
                                                )
                                            )
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Không thể mở ứng dụng ngoài",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    return true
                                }

                                // Để WebView tự tải các URL HTTP/HTTPS (không chuyển sang Chrome)
                                return false
                            }
                        }

                        // WebChromeClient: bắt window.open() của VNPay sandbox
                        // Nếu không có cái này, VNPay sandbox sẽ mở Chrome bên ngoài
                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                // Tạo WebView phụ để hứng popup, rồi redirect về WebView cha
                                val popupWebView = WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            popupView: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val popupUrl = request?.url.toString()
                                            // Callback từ VNPay → xử lý kết quả
                                            if (popupUrl.startsWith("gymapp://payment/result")) {
                                                val uri = android.net.Uri.parse(popupUrl)
                                                val bookingId = uri.getQueryParameter("booking_id") ?: ""
                                                onPaymentResultReceived(bookingId)
                                                return true
                                            }
                                            // Redirect HTTP/HTTPS → load vào WebView cha
                                            if (popupUrl.startsWith("http://") || popupUrl.startsWith("https://")) {
                                                view?.loadUrl(popupUrl)
                                                return true
                                            }
                                            return false
                                        }
                                    }
                                }
                                val transport = resultMsg?.obj as? WebView.WebViewTransport
                                transport?.webView = popupWebView
                                resultMsg?.sendToTarget()
                                return true
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
