package com.gymapp.modules.booking.dto;

import com.gymapp.modules.payment.enums.PaymentProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull
    @JsonProperty("pt_id")
    private UUID ptId;

    @NotNull
    @JsonProperty("availability_id")
    private UUID availabilityId;

    @NotNull
    @JsonProperty("payment_provider")
    private PaymentProvider paymentProvider;
}
