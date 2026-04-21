package com.gymapp.modules.notification.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.notification.dto.BroadcastNotificationRequest;
import com.gymapp.modules.notification.service.NotificationBroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/admin")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationBroadcastService broadcastService;

    @PostMapping("/broadcast")
    public ApiResponse<Void> broadcast(
            @Valid @RequestBody BroadcastNotificationRequest request
    ) {
        broadcastService.broadcast(request);
        return ApiResponse.ok(null, "Gửi broadcast thành công");
    }
}
