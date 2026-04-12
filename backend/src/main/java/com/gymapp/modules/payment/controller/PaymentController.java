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
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> allParams) {
        String paymentIdStr = allParams.get("vnp_TxnRef");
        return handleReturnUrl(PaymentProvider.VNPAY, allParams, paymentIdStr,
                paymentProperties.getVnpay().getMobileRedirectUrl());
    }

    @GetMapping("/momo-return")
    public ResponseEntity<Void> momoReturn(@RequestParam Map<String, String> allParams) {
        String paymentIdStr = allParams.get("orderId");
        return handleReturnUrl(PaymentProvider.MOMO, allParams, paymentIdStr,
                paymentProperties.getMomo().getMobileRedirectUrl());
    }

    private ResponseEntity<Void> handleReturnUrl(PaymentProvider provider, Map<String, String> params,
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

        return generateRedirectHtml(redirectUrl, "booking_id", bookingId);
    }

    private ResponseEntity<Void> generateRedirectHtml(String baseUri, String paramKey, String paramValue) {
        String finalUrl = baseUri + (baseUri.contains("?") ? "&" : "?") + paramKey + "=" + paramValue;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, finalUrl)
                .build();
    }

    @GetMapping("/alls")
    public List<PaymentResponse> getAllPayments() {
        return paymentService.getAllPayments();
    }

}
