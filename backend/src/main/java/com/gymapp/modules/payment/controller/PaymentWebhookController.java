package com.gymapp.modules.payment.controller;

import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;

    /**
     * Webhook endpoint for VNPay callback
     */
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Object> vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("Received VNPay webhook: {}", params);
        Object response = paymentService.confirmPayment(PaymentProvider.VNPAY, params);

        // VNPay expects JSON response with RspCode and Message for IPN confirmation
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook endpoint for MoMo callback
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoCallback(@RequestBody Map<String, String> params) {
        log.info("Received MoMo webhook: {}", params);
        paymentService.confirmPayment(PaymentProvider.MOMO, params);

        // MoMo expects 204 No Content for successful IPN
        return ResponseEntity.noContent().build();
    }
}
