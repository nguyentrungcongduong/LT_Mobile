package com.gymapp.modules.branch.service;

import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.branch.dto.BranchRequest;
import com.gymapp.modules.branch.dto.BranchResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BranchService {
    PageResponse<BranchResponse> getAllBranches(Pageable pageable);

    BranchResponse getBranchById(UUID id);

    BranchResponse createBranch(BranchRequest request);

    BranchResponse updateBranch(UUID id, BranchRequest request);

    void deleteBranch(UUID id);
}
