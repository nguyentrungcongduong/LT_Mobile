package com.gymapp.modules.user.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Data
@Getter
@Setter
@Builder
public class BlockUserRequest {
    private boolean active;
}
