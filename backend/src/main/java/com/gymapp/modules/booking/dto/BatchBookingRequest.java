package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.payment.enums.PaymentProvider;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchBookingRequest {

    @NotNull
    @JsonProperty("pt_id")
    private UUID ptId;

    @NotEmpty
    @JsonProperty("availability_ids")
    private List<UUID> availabilityIds;

    @NotNull
    @JsonProperty("payment_provider")
    private PaymentProvider paymentProvider;
}
