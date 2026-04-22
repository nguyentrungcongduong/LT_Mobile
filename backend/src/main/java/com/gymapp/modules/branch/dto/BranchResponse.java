package com.gymapp.modules.branch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private BigDecimal latitude;
    private BigDecimal longitude;
    // Force JSON key = "isActive" so it matches frontend Branch.isActive
    // (Without this, Lombok's isActive() getter causes Jackson to serialize as "active")
    @JsonProperty("isActive")
    private boolean isActive;
    private OffsetDateTime createdAt;
}
