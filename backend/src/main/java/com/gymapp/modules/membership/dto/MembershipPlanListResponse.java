package com.gymapp.modules.membership.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlanListResponse {
    private List<MembershipPlanResponse> plans;
}
