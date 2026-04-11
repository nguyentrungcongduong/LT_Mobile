package com.gymapp.modules.payment.service;

import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.dto.response.PaymentResponse;
import com.gymapp.modules.payment.dto.response.PaymentStatusResponse;
import com.gymapp.modules.payment.enums.PaymentProvider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    PaymentInitiateResponse initiatePayment(UUID userId, PaymentInitiateRequest request, String ipAddress);

    Object confirmPayment(PaymentProvider provider, Map<String, String> callbackData);

    PaymentStatusResponse getPaymentStatus(UUID paymentId);

    boolean processRefund(UUID paymentId, long amount, String reason);

    List<PaymentResponse> getAllPayments();
}
