package com.gymapp.modules.branch.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.branch.dto.BranchResponse;
import com.gymapp.modules.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branch", description = "Public branch operations")
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    @Operation(summary = "Get all active branches")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getAllBranches(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(branchService.getAllBranches(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by id")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable(name = "id") UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.getBranchById(id)));
    }
}
