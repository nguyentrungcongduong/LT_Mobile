package com.gymapp.modules.booking.service;

import com.gymapp.modules.booking.dto.PtAvailabilityRequest;
import com.gymapp.modules.booking.dto.PtAvailabilityResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    List<PtAvailabilityResponse> getPtAvailability(UUID ptId, LocalDate from, LocalDate to);
    PtAvailabilityResponse createAvailability(UUID ptId, PtAvailabilityRequest request);
}
