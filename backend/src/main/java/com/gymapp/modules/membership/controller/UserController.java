package com.gymapp.modules.membership.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gymapp.common.response.ApiResponse;
import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;
import com.gymapp.modules.membership.service.Interface.IUserService;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://127.0.0.1:5500")
@Builder
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public UserResponse getUser() {
        return userService.getUser();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAll")
    public ApiResponse<List<UserResponse>> getAllUser() {
        return ApiResponse.ok(userService.getAllUser(), "Lay thanh cong");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/createUser")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserDto userDto) {

        UserResponse user = userService.createUser(userDto);
        return ApiResponse.ok(user, "Tao thanh cong");
    }

    // UserResponse updateUser(UserDto userDto, UUID id);
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/id")
    public ApiResponse<UserResponse> updateUserById(@RequestBody @Valid UserDto userDto, @PathVariable UUID id) {
        UserResponse userResponse = userService.updateUser(userDto, id);
        return ApiResponse.ok(userResponse, "Updated");
    }

    // User getUserById(UUID id);
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUser/id")
    public ApiResponse<UserResponse> getUserById(@PathVariable UUID id) {
        return ApiResponse.ok(userService.getUserById(id), "Thanh cong");
    }

    // UserResponse updateCurrentUser(UserUpdateDto dto);
    @PutMapping("/update/me")
    public ApiResponse<UserResponse> updateCurrentUser(@RequestBody @Valid UserUpdateDto userDto) {
        UserResponse userResponse = userService.updateCurrentUser(userDto);
        return ApiResponse.ok(userResponse, "Updated");
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(file); // dùng method service đã chỉnh sửa để chỉ update user hiện tại
        return ResponseEntity.ok(url);
    }
}
