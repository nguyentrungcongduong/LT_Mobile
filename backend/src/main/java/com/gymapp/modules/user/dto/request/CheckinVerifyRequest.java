package com.gymapp.modules.user.dto.request;

import lombok.Data;

@Data
public class CheckinVerifyRequest {
    private String qrToken;
    private String branchId; // UUID as String (optional, for branch validation)
}
