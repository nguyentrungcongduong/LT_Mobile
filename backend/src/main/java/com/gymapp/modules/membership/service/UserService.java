package com.gymapp.modules.membership.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;
import com.gymapp.modules.membership.service.Interface.IUserService;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    @Override
    public UserResponse createUser(UserDto userDto) {
        User user = User.builder()
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .role(UserRole.USER)
                .phone(userDto.getPhone())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .build();
        userRepository.save(user);
        return UserResponse.fromUser(user);
    }

    // cho admin
    @Override
    public UserResponse updateUser(UserDto userDto, UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("404", "ko thấy User"));
        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
            ;
        }
        if (userDto.getEmail() != null) {
            user.setEmail(user.getEmail());
            ;
        }
        if (userDto.getPhone() != null) {
            user.setPhone(userDto.getFullName());
            ;
        }
        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }
        userRepository.save(user);
        return UserResponse.fromUser(user);
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> list = userRepository.findAll();
        return UserResponse.fromUserList(list);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("404", "Ko tim thay id"));
        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateDto dto) {
        // Lấy user hiện tại từ JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update các field được phép
        if (dto.getFullName() != null)
            user.setFullName(dto.getFullName());
        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        if (dto.getHeight() != null) {
            user.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            user.setWeight(dto.getWeight());
        }
        // Lưu DB
        User updated = userRepository.save(user);

        return UserResponse.fromUser(updated);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        // Lấy user hiện tại từ JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", "user_" + user.getId(),
                            "overwrite", true));

            String avatarUrl = (String) uploadResult.get("secure_url");

            // Lưu URL vào DB
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }
}
