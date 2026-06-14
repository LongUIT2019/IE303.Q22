package com.example.taxi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(FirestoreUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        "ROLE_ADMIN",
                        "admin@taxidispatch.com",
                        "System Admin",
                        "0000000000");
                admin.setActive(true);
                userRepository.save(admin);
                System.out.println("✅ Default Admin đã được tạo trên Firestore.");
            } else {
                System.out.println("✅ Admin đã tồn tại trên Firestore.");
            }
        };
    }
}
