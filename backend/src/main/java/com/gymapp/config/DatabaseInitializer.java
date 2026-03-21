package com.gymapp.config;

import com.gymapp.modules.user.entity.User;
import com.gymapp.modules.user.entity.UserRole;
import com.gymapp.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info(">>> START INIT DATABASE");

        if (userRepository.count() == 0) {
            log.info(">>> No users found. Initializing default ADMIN account...");

            User adminUser = User.builder()
                    .email("admin@gmail.com")
                    .passwordHash(passwordEncoder.encode("12345678"))
                    .fullName("Super Admin")
                    .phone("0123456789")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            userRepository.save(adminUser);
        } else {
            log.info(">>> Database already initialized (users count: {})", userRepository.count());
        }

        log.info(">>> END INIT DATABASE");
    }
}
