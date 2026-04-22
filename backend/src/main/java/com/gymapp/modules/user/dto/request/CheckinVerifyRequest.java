package com.gymapp.modules.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinVerifyRequest {
    private String qrToken;
    private String branchId; // UUID as String (optional, for branch validation)
}
