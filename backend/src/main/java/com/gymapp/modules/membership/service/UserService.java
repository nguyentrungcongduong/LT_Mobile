package com.gymapp.modules.membership.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gymapp.common.exception.ResourceNotFoundException;
import com.gymapp.modules.membership.dto.ChangePasswordRequest;
import com.gymapp.modules.membership.dto.UpdateFcmTokenRequest;
import com.gymapp.modules.membership.dto.UpdateUserGoalRequest;
import com.gymapp.modules.membership.dto.UserDto;
import com.gymapp.modules.membership.dto.UserResponse;
import com.gymapp.modules.membership.dto.UserUpdateDto;
import com.gymapp.modules.membership.service.Interface.IUserService;
import com.gymapp.modules.user.dto.request.BlockUserRequest;
import com.gymapp.modules.user.dto.response.UserStatusResponse;
import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CrossOrigin("*")
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    @Override
    public UserResponse createUser(UserDto userDto) {
        User user = User.builder()
                .fullName(userDto.getFullName())
                .email(userDto.getEmail())
                .role(userDto.getRole() != null ? userDto.getRole() : UserRole.USER) // admin có thể set role
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

        System.out.println("GetUser - Email: " + email + ", AvatarUrl: " + user.getAvatarUrl());

        return UserResponse.fromUser(user);
    }

    @Override
    public UserResponse updateCurrentUser(UserUpdateDto dto) {
        // Lấy user hiện tại từ JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        System.out.println("updateCurrentUser - Email: " + email + ", DTO: " + dto);
        System.out.println("DTO fullName: " + dto.getFullName() + ", phone: " + dto.getPhone() + ", avatarUrl: "
                + dto.getAvatarUrl());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update các field được phép
        if (dto.getFullName() != null) {
            System.out.println("Updating fullName from " + user.getFullName() + " to " + dto.getFullName());
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhone() != null) {
            System.out.println("Updating phone from " + user.getPhone() + " to " + dto.getPhone());
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            System.out.println("Updating email from " + user.getEmail() + " to " + dto.getEmail());
            user.setEmail(dto.getEmail());
        }
        if (dto.getAge() != null) {
            user.setAge(dto.getAge());
        }
        if (dto.getHeight() != null) {
            user.setHeight(dto.getHeight());
        }
        if (dto.getWeight() != null) {
            user.setWeight(dto.getWeight());
        }
        if (dto.getAvatarUrl() != null) {
            System.out.println("Updating avatarUrl from " + user.getAvatarUrl() + " to " + dto.getAvatarUrl());
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        // Lưu DB
        User updated = userRepository.save(user);
        System.out
                .println("Saved user - fullName: " + updated.getFullName() + ", avatarUrl: " + updated.getAvatarUrl());

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
            System.out.println("Uploading avatar for user: " + user.getId());

            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "avatars",
                            "public_id", "user_" + user.getId(),
                            "overwrite", true));

            String avatarUrl = (String) uploadResult.get("secure_url");
            System.out.println("Cloudinary upload success. URL: " + avatarUrl);

            // Lưu URL vào DB
            user.setAvatarUrl(avatarUrl);
            User saved = userRepository.save(user);
            System.out.println("Saved to DB. User avatarUrl: " + saved.getAvatarUrl());

            return avatarUrl;
        } catch (java.io.IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    public User updateMyGoal(UpdateUserGoalRequest request) {
        // Lấy thông tin user từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update nếu có dữ liệu
        if (request.getExperienceLevel() != null) {
            user.setExperienceLevel(request.getExperienceLevel());
        }

        if (request.getFitnessGoal() != null) {
            user.setFitnessGoal(request.getFitnessGoal());
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateFcmToken(UpdateFcmTokenRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));
        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);
    }

    public UserStatusResponse blockOrUnblockUser(UUID userId, BlockUserRequest request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("🔧 Before update: isActive = " + user.isActive());
            user.setActive(request.isActive());
            System.out.println("🔧 After setActive: isActive = " + user.isActive());

            User savedUser = userRepository.saveAndFlush(user);
            System.out.println("🔧 After save: isActive = " + savedUser.isActive());

            return UserStatusResponse.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .isActive(savedUser.isActive())
                    .message(savedUser.isActive() ? "User unblocked" : "User blocked")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
