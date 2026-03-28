package com.gymapp.modules.membership.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Double weight;
    private Double height;
    private Double age;
    private String avatarUrl;

    public static UserResponse fromUser(User user) {
        if (user == null)
            return null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    public static List<UserResponse> fromUserList(List<User> users) {
        return users.stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }
}
