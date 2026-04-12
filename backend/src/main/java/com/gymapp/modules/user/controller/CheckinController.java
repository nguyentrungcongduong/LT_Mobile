package com.gymapp.modules.checkin.controller;

import com.gymapp.modules.checkin.dto.CheckinRequest;
import com.gymapp.modules.checkin.service.CheckinService;
import com.gymapp.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public String checkin(@RequestBody CheckinRequest request) {
        return checkinService.checkin(request.getQrData());
    }
}