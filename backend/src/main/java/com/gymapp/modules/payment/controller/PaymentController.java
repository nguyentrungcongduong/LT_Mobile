package com.gymapp.modules.payment.controller;

import com.gymapp.common.exception.UnauthorizedException;
import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.security.JwtUtil;
import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.dto.response.PaymentStatusResponse;
import com.gymapp.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final com.gymapp.config.PaymentProperties paymentProperties;

    @org.springframework.beans.factory.annotation.Value("classpath:templates/payment-redirect.html")
    private org.springframework.core.io.Resource redirectTemplate;

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

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> allParams) {
        String paymentId = allParams.get("vnp_TxnRef");
        String redirectUrl = paymentProperties.getVnpay().getMobileRedirectUrl();
        return generateRedirectHtml(redirectUrl, paymentId);
    }

    @GetMapping("/momo-return")
    public ResponseEntity<String> momoReturn(@RequestParam Map<String, String> allParams) {
        String paymentId = allParams.get("orderId");
        String redirectUrl = paymentProperties.getMomo().getMobileRedirectUrl();
        return generateRedirectHtml(redirectUrl, paymentId);
    }

    private ResponseEntity<String> generateRedirectHtml(String baseUri, String paymentId) {
        String finalUrl = baseUri + (baseUri.contains("?") ? "&" : "?") + "paymentId=" + paymentId;

        try {
            String html = org.springframework.util.StreamUtils.copyToString(
                    redirectTemplate.getInputStream(),
                    java.nio.charset.StandardCharsets.UTF_8);

            html = html.replace("{{finalUrl}}", finalUrl);

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body(html);
        } catch (java.io.IOException e) {
            log.error("Error loading redirect template", e);
            return ResponseEntity.internalServerError().body("Error redirecting to app");
        }
    }
}
