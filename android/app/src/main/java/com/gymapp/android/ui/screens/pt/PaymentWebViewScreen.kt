package com.gymapp.android.ui.screens.pt

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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

            // Dùng FrameLayout làm container để stack nhiều WebView (main + popup)
            // BẮT BUỘC: popup WebView phải được addView vào đây thì mới nhận được input
            AndroidView(
                factory = { context ->
                    val frameLayout = FrameLayout(context)

                    // Helper: tạo WebView với đầy đủ settings cần thiết cho VNPay/MoMo
                    fun buildWebView(onPageDone: (() -> Unit)? = null): WebView {
                        return WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                setSupportMultipleWindows(true)
                                javaScriptCanOpenWindowsAutomatically = true
                                // VNPay load mix HTTP + HTTPS (logo ngân hàng, redirect...)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                @Suppress("DEPRECATION")
                                saveFormData = true
                            }

                            // BẮT BUỘC: bật cookie để VNPay submit form hoạt động
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            webViewClient = object : WebViewClient() {
                                // Bỏ qua SSL error của VNPay sandbox (dùng self-signed cert)
                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    handler?.proceed() // Cho phép load dù SSL không hợp lệ
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    onPageDone?.invoke()
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val loadingUrl = request?.url.toString()

                                    // Bypass trang cảnh báo của ngrok (hiện sau khi VNPay redirect về return-url)
                                    // Ngrok chặn browser access → cần header "ngrok-skip-browser-warning"
                                    if (loadingUrl.contains("ngrok-free.dev") || loadingUrl.contains("ngrok.io")) {
                                        view?.loadUrl(
                                            loadingUrl,
                                            mapOf("ngrok-skip-browser-warning" to "true")
                                        )
                                        return true
                                    }

                                    // Bắt deep link callback thanh toán thành công/thất bại
                                    if (loadingUrl.startsWith("gymapp://payment/result")) {
                                        val uri = Uri.parse(loadingUrl)
                                        val bookingId = uri.getQueryParameter("booking_id") ?: ""
                                        onPaymentResultReceived(bookingId)
                                        return true
                                    }

                                    // Bắt các scheme không phải HTTP/HTTPS (vnpay://, momo://, intent://…)
                                    if (!loadingUrl.startsWith("http://") && !loadingUrl.startsWith("https://")) {
                                        try {
                                            if (loadingUrl.startsWith("intent://")) {
                                                val intent = Intent.parseUri(
                                                    loadingUrl,
                                                    Intent.URI_INTENT_SCHEME
                                                )
                                                val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                                when {
                                                    intent.resolveActivity(context.packageManager) != null ->
                                                        context.startActivity(intent)
                                                    fallbackUrl != null ->
                                                        view?.loadUrl(fallbackUrl)
                                                    intent.`package` != null ->
                                                        context.startActivity(
                                                            Intent(
                                                                Intent.ACTION_VIEW,
                                                                Uri.parse("market://details?id=${intent.`package`}")
                                                            )
                                                        )
                                                }
                                            } else {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(loadingUrl))
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

                                    // HTTP/HTTPS → để WebView tự tải (KHÔNG mở Chrome ngoài)
                                    return false
                                }
                            }
                        }
                    }

                    // ── Main WebView ─────────────────────────────────────────
                    val mainWebView = buildWebView(onPageDone = { isLoading = false })

                    mainWebView.webChromeClient = object : android.webkit.WebChromeClient() {
                        /**
                         * VNPay sandbox dùng window.open() để mở trang OTP/3DS của ngân hàng.
                         * Phải addView popup vào frameLayout → popup mới hiển thị và nhận input.
                         * Nếu không addView → popup WebView tồn tại nhưng bị detached → click không ăn.
                         */
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val popupWebView = buildWebView()

                            // Cho popup tự đóng (khi ngân hàng gọi window.close())
                            popupWebView.webChromeClient = object : android.webkit.WebChromeClient() {
                                override fun onCloseWindow(window: WebView?) {
                                    frameLayout.removeView(window)
                                }
                            }

                            // ADD vào FrameLayout - đây là điều quan trọng nhất!
                            frameLayout.addView(
                                popupWebView,
                                FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                )
                            )

                            // Kết nối popup WebView với message transport của VNPay
                            val transport = resultMsg?.obj as? WebView.WebViewTransport
                            transport?.webView = popupWebView
                            resultMsg?.sendToTarget()
                            return true
                        }

                        override fun onCloseWindow(window: WebView?) {
                            frameLayout.removeView(window)
                        }
                    }

                    // Add main WebView vào container trước
                    frameLayout.addView(
                        mainWebView,
                        FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    )

                    mainWebView.loadUrl(url)
                    frameLayout
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
