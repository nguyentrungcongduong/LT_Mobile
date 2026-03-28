package com.gymapp.modules.membership.dto;

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
public class UserUpdateDto {
    private String fullName;
    private String phone;
    private Double weight;
    private Double height;
    private Double age;
    private String avatarUrl;
}
