package com.gymapp.modules.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtProfileCreateReq {
    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "Specializations are required")
    private List<String> specializations;

    @NotNull(message = "Price per session is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal pricePerSession;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsExperience;

    private List<String> certificateUrls;
}
