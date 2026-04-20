package com.gymapp.modules.branch.controller;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.branch.dto.BranchRequest;
import com.gymapp.modules.branch.dto.BranchResponse;
import com.gymapp.modules.branch.service.BranchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/branches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Branch", description = "Admin branch operations")
public class AdminBranchController {

    private final BranchService branchService;

    @PostMapping
    @Operation(summary = "Create new branch")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody BranchRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(branchService.updateBranch(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete branch")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable(name = "id") UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Branch deleted successfully"));
    }
}
