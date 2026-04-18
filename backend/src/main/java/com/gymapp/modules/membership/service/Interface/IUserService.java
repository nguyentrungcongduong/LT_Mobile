package com.gymapp.modules.membership.service.Interface;

import java.util.List;
import java.util.UUID;

import com.gymapp.modules.membership.dto.UpdateFcmTokenRequest;

import org.springframework.web.multipart.MultipartFile;

import com.gymapp.modules.membership.dto.UpdateUserGoalRequest;
import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;
import com.gymapp.modules.membership.dto.ChangePasswordRequest;
import com.gymapp.modules.user.dto.request.BlockUserRequest;
import com.gymapp.modules.user.dto.response.UserStatusResponse;
import com.gymapp.modules.user.entity.User;

public interface IUserService {
    UserResponse createUser(UserDto userDto);

    UserResponse updateUser(UserDto userDto, UUID id);

    List<UserResponse> getAllUser();

    UserResponse getUser();

    UserResponse getUserById(UUID id);

    UserResponse updateCurrentUser(UserUpdateDto dto);

    String uploadAvatar(MultipartFile file);

    User updateMyGoal(UpdateUserGoalRequest request);

    void changePassword(ChangePasswordRequest request);

    UserStatusResponse blockOrUnblockUser(UUID userId, BlockUserRequest request);

    void updateFcmToken(UpdateFcmTokenRequest request);
}
