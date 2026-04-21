package com.gymapp.modules.payment.dto.response;

import com.gymapp.modules.payment.enums.PaymentProvider;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.enums.PaymentType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class PaymentHistoryResponse {
    private UUID paymentId;
    private String transactionName;
    private PaymentProvider provider;
    private PaymentType paymentType;
    private PaymentStatus status;
    private BigDecimal amount;
    private OffsetDateTime createdAt;
}
