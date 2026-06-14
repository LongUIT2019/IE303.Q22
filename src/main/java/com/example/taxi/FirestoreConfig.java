package com.example.taxi;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.Resource;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirestoreConfig {

    @Value("${firebase.credential.path}")
    private Resource credentialPath;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = credentialPath.getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            System.err.println("⚠ Chưa tìm thấy file firebase-key.json, hệ thống sẽ chạy offline / báo lỗi khi dùng Firestore.");
            System.err.println(e.getMessage());
        }
    }

    @Bean
    public Firestore getFirestore() {
        try {
            return FirestoreClient.getFirestore();
        } catch (Exception e) {
            return null; // Return null if not initialized correctly
        }
    }
}
