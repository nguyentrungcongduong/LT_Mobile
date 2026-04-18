package com.gymapp.modules.membership.service;

import com.gymapp.modules.membership.dto.response.ActiveMembershipResponse;
import java.util.UUID;

public interface UserMembershipService {
    ActiveMembershipResponse getActiveMembershipForCurrentUser();
    ActiveMembershipResponse registerMembership(UUID planId);
}
