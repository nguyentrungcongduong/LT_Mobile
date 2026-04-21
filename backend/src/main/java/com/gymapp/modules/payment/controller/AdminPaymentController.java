package com.gymapp.modules.payment.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.payment.dto.response.PaymentAdminResponse;
import com.gymapp.modules.payment.dto.response.RefundAdminResponse;
import com.gymapp.modules.payment.enums.PaymentStatus;
import com.gymapp.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Payment", description = "Admin payment operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payments")
    @Operation(summary = "Get transaction history for admin with filters")
    public ResponseEntity<ApiResponse<PageResponse<PaymentAdminResponse>>> getPaymentHistory(
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(name = "status", required = false) PaymentStatus status,
            @RequestParam(name = "userName", required = false) String userName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = paymentService.getAdminPaymentHistory(startDate, endDate, status, userName, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/refunds")
    @Operation(summary = "Get refund history for admin")
    public ResponseEntity<ApiResponse<PageResponse<RefundAdminResponse>>> getRefundHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = paymentService.getAdminRefundHistory(pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
