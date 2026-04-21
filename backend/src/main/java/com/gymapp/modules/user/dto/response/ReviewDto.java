package com.gymapp.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private String avatarUrl;
    private Integer rating;
    private String comment;
    private OffsetDateTime createdAt;
}
