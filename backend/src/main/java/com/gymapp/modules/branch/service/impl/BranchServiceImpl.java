package com.gymapp.modules.branch.service.impl;

import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.common.response.PageResponse;
import com.gymapp.modules.branch.dto.BranchRequest;
import com.gymapp.modules.branch.dto.BranchResponse;
import com.gymapp.modules.branch.entity.Branch;
import com.gymapp.modules.branch.repository.BranchRepository;
import com.gymapp.modules.branch.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BranchResponse> getAllBranches(Pageable pageable) {
        Page<Branch> branchPage = branchRepository.findAll(pageable);

        List<BranchResponse> items = branchPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<BranchResponse>builder()
                .items(items)
                .pagination(PageResponse.PaginationMeta.builder()
                        .page(branchPage.getNumber())
                        .limit(branchPage.getSize())
                        .total(branchPage.getTotalElements())
                        .totalPages(branchPage.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("BRANCH_NOT_FOUND", "Branch not found with id: " + id));
        return mapToResponse(branch);
    }

    @Override
    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return mapToResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("BRANCH_NOT_FOUND", "Branch not found with id: " + id));

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());
        if (request.getIsActive() != null) {
            branch.setActive(request.getIsActive());
        }

        return mapToResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(UUID id) {
        if (!branchRepository.existsById(id)) {
            throw new ResourceNotFoundException("BRANCH_NOT_FOUND", "Branch not found with id: " + id);
        }
        branchRepository.deleteById(id);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .isActive(branch.isActive())
                .createdAt(branch.getCreatedAt())
                .build();
    }
}
