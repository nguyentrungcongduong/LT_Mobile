package com.gymapp.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientProgressDto {
    @JsonProperty("sessions")
    private List<SessionHistoryDto> sessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionHistoryDto {
        @JsonProperty("booking_id")
        private UUID bookingId;

        @JsonProperty("date")
        private OffsetDateTime date;

        @JsonProperty("status")
        private String status;

        @JsonProperty("workout_logs")
        private List<WorkoutLogDto> workoutLogs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutLogDto {
        @JsonProperty("exercise_name")
        private String exerciseName;

        @JsonProperty("notes")
        private String notes;

        @JsonProperty("sets")
        private Integer sets;

        @JsonProperty("reps")
        private Integer reps;

        @JsonProperty("weight")
        private java.math.BigDecimal weight;
    }
}
