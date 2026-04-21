package com.gymapp.modules.booking.service.impl;

import com.gymapp.common.exception.BadRequestException;
import com.gymapp.common.exception.ConflictException;
import com.gymapp.modules.booking.dto.PtAvailabilityRequest;
import com.gymapp.modules.booking.dto.PtAvailabilityResponse;

import com.gymapp.modules.booking.entity.Booking;

import com.gymapp.modules.booking.entity.PtAvailability;
import com.gymapp.modules.booking.enums.BookingStatus;
import com.gymapp.modules.booking.repository.BookingRepository;
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
    private final com.gymapp.modules.user.repository.UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PtAvailabilityResponse> getPtAvailability(UUID ptIdOrUserId, LocalDate from, LocalDate to) {
        UUID userId = ptIdOrUserId;

        // The frontend might pass either PtProfile ID (from user flow) or User ID (from
        // PT flow)
        // Since UUIDs are globally unique, we can check if it's a profile ID first
        java.util.Optional<com.gymapp.modules.user.entity.PtProfile> profileOpt = ptProfileRepository
                .findById(ptIdOrUserId);
        // The frontend might pass either PtProfile ID (from user flow) or User ID (from PT flow)
        // Since UUIDs are globally unique, we can check if it's a profile ID first

        if (profileOpt.isPresent()) {
            userId = profileOpt.get().getUser().getId();
        }

        return availabilityRepository.findAllByPtIdAndAvailableDateBetween(userId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PtAvailabilityResponse createAvailability(UUID ptId, PtAvailabilityRequest request) {
        // 0. Auto-create PT profile if it doesn't exist yet
        if (ptProfileRepository.findByUserId(ptId).isEmpty()) {
            com.gymapp.modules.user.entity.User ptUser = userRepository.findById(ptId)
                    .orElseThrow(() -> new BadRequestException("USER_NOT_FOUND", "User not found"));
            com.gymapp.modules.user.entity.PtProfile newProfile = com.gymapp.modules.user.entity.PtProfile.builder()
                    .user(ptUser)
                    .bio("")
                    .pricePerSession(java.math.BigDecimal.ZERO)
                    .build();
            ptProfileRepository.save(newProfile);
        }

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
        PtAvailabilityResponse.PtAvailabilityResponseBuilder builder = PtAvailabilityResponse.builder()
                .id(a.getId())
                .availableDate(a.getAvailableDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .isBooked(a.isBooked());

        // Nếu slot đã có booking, lấy thông tin học viên
        if (a.isBooked()) {
            bookingRepository.findFirstByAvailabilityIdAndStatusIn(
                    a.getId(),
                    List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)).ifPresent(booking -> {
                        if (booking.getUser() != null) {
                            builder.bookedByName(booking.getUser().getFullName());
                            builder.bookedByAvatar(booking.getUser().getAvatarUrl());
                            builder.bookingId(booking.getId());
                        }
                    });
        }

        return builder.build();
    }
}
