package com.gymapp.modules.payment.dto.response;

import com.gymapp.modules.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentStatusResponse {
    private UUID paymentId;
    private PaymentStatus status;
    private String bookingStatus;
}
