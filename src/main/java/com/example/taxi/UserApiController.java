package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private FirestoreUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        return userRepository.findByUsername(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication auth, @RequestBody AppUser updatedInfo) {
        if (auth == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        try {
            AppUser user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setFullName(updatedInfo.getFullName());
            user.setEmail(updatedInfo.getEmail());
            user.setPhoneNumber(updatedInfo.getPhoneNumber());

            if (updatedInfo.getPassword() != null && !updatedInfo.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedInfo.getPassword()));
            }

            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/online")
    public ResponseEntity<?> setOnlineStatus(Authentication auth, @RequestParam boolean online) {
        if (auth == null) return ResponseEntity.status(401).body("Chưa đăng nhập");
        try {
            AppUser user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setOnline(online);
            userRepository.save(user);
            return ResponseEntity.ok("Status updated: " + online);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
}
