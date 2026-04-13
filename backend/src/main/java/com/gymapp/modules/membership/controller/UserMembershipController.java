package com.gymapp.modules.membership.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.membership.dto.response.ActiveMembershipResponse;
import com.gymapp.modules.membership.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class UserMembershipController {

    private final UserMembershipService userMembershipService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ApiResponse<ActiveMembershipResponse> getMyActiveMembership() {
        return ApiResponse.ok(userMembershipService.getActiveMembershipForCurrentUser());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/register/{planId}")
    public ApiResponse<ActiveMembershipResponse> registerMembership(@PathVariable UUID planId) {
        return ApiResponse.ok(userMembershipService.registerMembership(planId));
    }
}
