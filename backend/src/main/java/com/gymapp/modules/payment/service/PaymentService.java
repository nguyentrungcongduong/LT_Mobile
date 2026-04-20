package com.gymapp.modules.payment.service;

import com.gymapp.common.response.PageResponse;

import com.gymapp.modules.payment.dto.request.PaymentInitiateRequest;
import com.gymapp.modules.payment.dto.response.PaymentAdminResponse;
import com.gymapp.modules.payment.dto.response.PaymentHistoryResponse;
import com.gymapp.modules.payment.dto.response.PaymentInitiateResponse;
import com.gymapp.modules.payment.dto.response.PaymentResponse;
import com.gymapp.modules.payment.dto.response.PaymentStatusResponse;
import com.gymapp.modules.payment.dto.response.RefundAdminResponse;
import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentInitiateResponse initiatePayment(UUID userId, PaymentInitiateRequest request, String ipAddress);

    Object confirmPayment(PaymentProvider provider, Map<String, String> callbackData);

    PaymentStatusResponse getPaymentStatus(UUID paymentId);

    Page<PaymentHistoryResponse> getPaymentHistory(
            UUID userId,
            PaymentType paymentType,
            PaymentStatus status,
            Pageable pageable);

    PageResponse<PaymentAdminResponse> getAdminPaymentHistory(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            PaymentStatus status,
            String userName,
            Pageable pageable);

    PageResponse<RefundAdminResponse> getAdminRefundHistory(Pageable pageable);

    boolean processRefund(UUID paymentId, long amount, String reason);

    List<PaymentResponse> getAllPayments();
}
