package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PtAvailabilityRequest {

    @NotNull(message = "Available date is required")
    @JsonProperty("available_date")
    private LocalDate availableDate;

    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.gymapp.config.MultiFormatLocalTimeDeserializer.class)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonProperty("end_time")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.gymapp.config.MultiFormatLocalTimeDeserializer.class)
    private LocalTime endTime;
}
