package com.gymapp.modules.booking.service.impl;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ConflictException;
import com.gymapp.modules.booking.dto.PtAvailabilityRequest;
import com.gymapp.modules.booking.dto.PtAvailabilityResponse;
import com.gymapp.modules.booking.entity.PtAvailability;
import com.gymapp.modules.booking.repository.PtAvailabilityRepository;
import com.gymapp.modules.booking.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final PtAvailabilityRepository availabilityRepository;
    private final com.gymapp.modules.user.repository.PtProfileRepository ptProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PtAvailabilityResponse> getPtAvailability(UUID ptProfileId, LocalDate from, LocalDate to) {
        // Resolve User ID from PT Profile ID
        com.gymapp.modules.user.entity.PtProfile profile = ptProfileRepository.findById(ptProfileId)
                .orElseThrow(() -> new com.gymapp.common.exception.ResourceNotFoundException("PT_PROFILE_NOT_FOUND", "Personal Trainer profile not found"));
        
        UUID ptUserId = profile.getUser().getId();

        return availabilityRepository.findAllByPtIdAndAvailableDateBetween(ptUserId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PtAvailabilityResponse createAvailability(UUID ptId, PtAvailabilityRequest request) {
        // 1. Validate date not in the past
        if (request.getAvailableDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("PAST_DATE", "Cannot set availability in the past");
        }

        // 2. Validate end_time > start_time
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("INVALID_TIME_RANGE", "End time must be after start time");
        }

        // 3. Check for overlapping slots
        boolean isOverlapping = availabilityRepository.existsOverlapping(
                ptId,
                request.getAvailableDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (isOverlapping) {
            throw new ConflictException("SLOT_ALREADY_EXISTS", "This time slot overlaps with an existing one");
        }

        // 4. Save
        PtAvailability availability = PtAvailability.builder()
                .ptId(ptId)
                .availableDate(request.getAvailableDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        PtAvailability saved = availabilityRepository.save(availability);

        return mapToResponse(saved);
    }

    private PtAvailabilityResponse mapToResponse(PtAvailability a) {
        return PtAvailabilityResponse.builder()
                .id(a.getId())
                .availableDate(a.getAvailableDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .isBooked(a.isBooked())
                .build();
    }
}
