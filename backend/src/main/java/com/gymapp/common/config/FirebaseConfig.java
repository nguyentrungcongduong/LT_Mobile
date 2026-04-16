package com.gymapp.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Khởi tạo Firebase Admin SDK khi ứng dụng start.
 *
 * Cần đặt file service-account JSON tại đường dẫn được cấu hình
 * trong application.yml: firebase.service-account-path
 *
 * Hoặc set biến môi trường FIREBASE_SERVICE_ACCOUNT_PATH.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp already initialized");
            return;
        }

        try {
            InputStream serviceAccount = loadServiceAccount();
            if (serviceAccount == null) {
                log.warn("Firebase service account file not found at: {}. FCM push notifications will be disabled.",
                        serviceAccountPath);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully");

        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK: {}. FCM push notifications will be disabled.",
                    e.getMessage());
        }
    }

    private InputStream loadServiceAccount() throws IOException {
        // 1. Thử load từ classpath (resources/)
        InputStream classpathStream = getClass().getClassLoader()
                .getResourceAsStream(serviceAccountPath);
        if (classpathStream != null) {
            log.info("Loaded Firebase service account from classpath: {}", serviceAccountPath);
            return classpathStream;
        }

        // 2. Thử load từ file system (absolute path)
        try {
            FileInputStream fileStream = new FileInputStream(serviceAccountPath);
            log.info("Loaded Firebase service account from filesystem: {}", serviceAccountPath);
            return fileStream;
        } catch (IOException ignored) {
            return null;
        }
    }
}
