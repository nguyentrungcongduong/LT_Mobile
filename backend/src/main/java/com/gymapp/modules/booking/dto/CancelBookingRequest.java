package com.gymapp.modules.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Data
@Getter
@Setter
@Builder
public class CancelBookingRequest {
    @NotBlank
    private String reason;
}
