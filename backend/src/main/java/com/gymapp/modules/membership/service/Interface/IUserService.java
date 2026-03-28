package com.gymapp.modules.membership.service.Interface;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;

public interface IUserService {
    UserResponse createUser(UserDto userDto);

    UserResponse updateUser(UserDto userDto, UUID id);

    List<UserResponse> getAllUser();

    UserResponse getUser();

    UserResponse getUserById(UUID id);

    UserResponse updateCurrentUser(UserUpdateDto dto);

    String uploadAvatar(MultipartFile file);
}
