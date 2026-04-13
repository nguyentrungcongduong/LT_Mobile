package com.gymapp.modules.membership.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymapp.modules.membership.enums.MembershipStatus;
import com.gymapp.modules.membership.enums.PlanType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ActiveMembershipResponse {
    private UUID id;
    
    @JsonProperty("plan_name")
    private String planName;
    
    @JsonProperty("plan_type")
    private PlanType planType;
    
    @JsonProperty("branch_name")
    private String branchName;
    
    private MembershipStatus status;
    
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @JsonProperty("end_date")
    private LocalDate endDate;
    
    @JsonProperty("days_left")
    private long daysLeft;
}
