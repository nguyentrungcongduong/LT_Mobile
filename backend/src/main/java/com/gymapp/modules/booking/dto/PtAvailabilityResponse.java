package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class PtAvailabilityResponse {
    private UUID id;

    @JsonProperty("available_date")
    private LocalDate availableDate;

    @JsonProperty("start_time")
    private LocalTime startTime;

    @JsonProperty("end_time")
    private LocalTime endTime;

    @JsonProperty("is_booked")
    private boolean isBooked;

    /** Tên học viên đã đặt slot này (null nếu còn trống) */
    @JsonProperty("booked_by_name")
    private String bookedByName;

    /** Avatar URL của học viên (null nếu còn trống) */
    @JsonProperty("booked_by_avatar")
    private String bookedByAvatar;

    /** booking_id tương ứng (null nếu còn trống) */
    @JsonProperty("booking_id")
    private UUID bookingId;
}
