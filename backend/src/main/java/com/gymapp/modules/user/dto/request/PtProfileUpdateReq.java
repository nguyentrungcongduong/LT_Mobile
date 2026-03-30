package com.gymapp.modules.user.dto.request;

import jakarta.validation.constraints.Min;
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
public class PtProfileUpdateReq {
    private String bio;
    private List<String> specializations;

    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal pricePerSession;

    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsExperience;

    private List<String> certificateUrls;
}
