package com.gymapp.modules.membership.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;

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
import com.gymapp.modules.membership.dto.UpdateUserGoalRequest;
import com.gymapp.modules.membership.dto.ChangePasswordRequest;
import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;
import com.gymapp.modules.membership.service.Interface.IUserService;
import com.gymapp.modules.user.entity.User;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Builder
public class UserController {
    private final IUserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getUser() {
        return ApiResponse.ok(userService.getUser(), "Lấy thông tin thành công");
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
    @PutMapping("/update/{id}")
    public ApiResponse<UserResponse> updateUserById(@RequestBody @Valid UserDto userDto,
            @PathVariable(name = "id") UUID id) {
        UserResponse userResponse = userService.updateUser(userDto, id);
        return ApiResponse.ok(userResponse, "Updated");
    }

    // User getUserById(UUID id);
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getUser/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable(name = "id") UUID id) {
        return ApiResponse.ok(userService.getUserById(id), "Thanh cong");
    }

    // UserResponse updateCurrentUser(UserUpdateDto dto);
    @PutMapping("/me")
    public ApiResponse<UserResponse> updateCurrentUser(@RequestBody @Valid UserUpdateDto userDto) {
        UserResponse userResponse = userService.updateCurrentUser(userDto);
        return ApiResponse.ok(userResponse, "Updated");
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<java.util.Map<String, String>> uploadAvatar(
            @Parameter(description = "Avatar image file", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary"))) @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(file); // dùng method service đã chỉnh sửa để chỉ update user hiện tại
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("avatar_url", url);
        return ApiResponse.ok(data, "Upload successful");
    }

    @PutMapping("/me/goal")
    public ApiResponse<UserResponse> updateMyGoal(@RequestBody UpdateUserGoalRequest request) {
        User user = userService.updateMyGoal(request);
        return ApiResponse.ok(UserResponse.fromUser(user));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.ok(null, "Đổi mật khẩu thành công");
    }
}
