package com.gymapp.modules.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Data
@Getter
@Setter
@Builder
public class CheckinVerifyRequest {
    private String qrToken;
    private String branchId; // UUID as String (optional, for branch validation)
}
