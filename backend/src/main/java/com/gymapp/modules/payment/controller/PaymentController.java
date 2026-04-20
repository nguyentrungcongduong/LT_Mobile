package com.gymapp.modules.payment.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentHistoryResponse;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.dto.response.PaymentResponse;
import com.gymapp.modules.payment.dto.response.PaymentStatusResponse;
import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;
import com.gymapp.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final com.gymapp.config.PaymentProperties paymentProperties;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for booking or membership")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiateRequest request,
            HttpServletRequest servletRequest) {

        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));

        String ipAddress = servletRequest.getRemoteAddr();

        PaymentInitiateResponse response = paymentService.initiatePayment(userId, request, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{paymentId}/status")
    @Operation(summary = "Get payment status polling")
    public ResponseEntity<ApiResponse<PaymentStatusResponse>> getPaymentStatus(
            @PathVariable UUID paymentId) {
        PaymentStatusResponse response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/history")
    @Operation(summary = "Get payment history for current user")
    public ResponseEntity<ApiResponse<Page<PaymentHistoryResponse>>> getPaymentHistory(
            @RequestParam(required = false) PaymentType paymentType,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UUID userId = UUID.fromString(JwtUtil.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not logged in")));

        var pageable = PageRequest.of(page, size);
        var result = paymentService.getPaymentHistory(userId, paymentType, status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> allParams) {
        String paymentIdStr = allParams.get("vnp_TxnRef");
        return handleReturnUrl(PaymentProvider.VNPAY, allParams, paymentIdStr,
                paymentProperties.getVnpay().getMobileRedirectUrl());
    }

    @GetMapping("/momo-return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> allParams) {
        String paymentIdStr = allParams.get("orderId");
        return handleReturnUrl(PaymentProvider.MOMO, allParams, paymentIdStr,
                paymentProperties.getMomo().getMobileRedirectUrl());
    }

    private ResponseEntity<String> handleReturnUrl(PaymentProvider provider, Map<String, String> params,
            String paymentIdStr, String redirectUrl) {
        try {
            paymentService.confirmPayment(provider, params);
        } catch (Exception e) {
            log.error("Error confirming payment synchronously in returnUrl: {}", e.getMessage());
        }

        String bookingId = "";
        try {
            PaymentStatusResponse status = paymentService.getPaymentStatus(UUID.fromString(paymentIdStr));
            bookingId = status.getBookingId() != null ? status.getBookingId().toString() : "";
        } catch (Exception e) {
            log.warn("Failed to retrieve bookingId for payment: {}", paymentIdStr);
        }

        return generateDeepLinkPage(redirectUrl, "booking_id", bookingId);
    }

    private ResponseEntity<String> generateDeepLinkPage(String baseUri, String paramKey, String paramValue) {
        String deepLinkUrl = baseUri + (baseUri.contains("?") ? "&" : "?") + paramKey + "=" + paramValue;

        String html = "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Đang xử lý thanh toán...</title>" +
                "<style>" +
                "body{font-family:sans-serif;display:flex;flex-direction:column;align-items:center;" +
                "justify-content:center;min-height:100vh;margin:0;background:#f0f4f8;color:#333;}" +
                "h2{margin-bottom:8px;}" +
                "p{color:#666;font-size:14px;}" +
                ".btn{margin-top:24px;padding:14px 28px;background:#1976D2;color:#fff;" +
                "border:none;border-radius:8px;font-size:16px;cursor:pointer;text-decoration:none;}" +
                "</style>" +
                "<script>" +
                "window.onload = function() {" +
                "  window.location.href = '" + deepLinkUrl + "';" +
                "  setTimeout(function() {" +
                "    document.getElementById('manual-btn').style.display = 'inline-block';" +
                "  }, 2000);" +
                "};" +
                "</script>" +
                "</head><body>" +
                "<h2>✅ Thanh toán thành công!</h2>" +
                "<p>Đang chuyển bạn về ứng dụng...</p>" +
                "<a id=\"manual-btn\" class=\"btn\" href=\"" + deepLinkUrl + "\" style=\"display:none\">" +
                "Quay về ứng dụng</a>" +
                "</body></html>";

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    @GetMapping("/alls")
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

}
