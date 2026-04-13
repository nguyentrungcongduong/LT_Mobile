package com.gymapp.modules.checkin.controller;

import com.gymapp.modules.checkin.dto.CheckinRequest;
import com.gymapp.modules.checkin.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public ResponseEntity<?> checkin(@RequestBody CheckinRequest request) {
        try {
            String result = checkinService.checkin(request.getQrData());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {

            if ("Unauthorized".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(e.getMessage());
            }

            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}
