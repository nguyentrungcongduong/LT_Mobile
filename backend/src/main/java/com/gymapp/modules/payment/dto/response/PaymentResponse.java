package com.gymapp.modules.payment.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class PaymentResponse {

    private UUID paymentId;
    private UUID bookingId;
    private UUID userId;

    private BigDecimal amount;
    private String currency;

    private PaymentType paymentType;
    private PaymentStatus status;
    private PaymentProvider provider;

    private String transactionId;
    private String gatewayUrl;

    private OffsetDateTime paidAt;
    private OffsetDateTime createdAt;
}
