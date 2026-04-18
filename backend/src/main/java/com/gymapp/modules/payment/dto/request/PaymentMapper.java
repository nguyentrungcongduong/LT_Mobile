package com.gymapp.modules.payment.dto.request;

import com.gymapp.modules.payment.dto.response.PaymentResponse;
import com.gymapp.modules.payment.entity.Payment;

public class PaymentMapper {
    private PaymentMapper() {
        // tránh new object
    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null)
            return null;

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .provider(payment.getProvider())
                .transactionId(payment.getTransactionId())
                .gatewayUrl(payment.getGatewayUrl())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
