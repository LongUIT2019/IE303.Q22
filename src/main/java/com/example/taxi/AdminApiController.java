package com.example.taxi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @Autowired
    private FirestoreUserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private FirestoreService firestoreService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AppUser newUser) {
        try {
            if (userRepository.existsByUsername(newUser.getUsername())) {
                return ResponseEntity.badRequest().body("Username đã tồn tại!");
            }
            // Mã hóa mật khẩu mặc định (ví dụ: 123456 hoặc mật khẩu từ request)
            String rawPassword = newUser.getPassword() != null ? newUser.getPassword() : "123456";
            newUser.setPassword(passwordEncoder.encode(rawPassword));
            newUser.setActive(true);
            
            userRepository.save(newUser);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi tạo user: " + e.getMessage());
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers() {
        try {
            List<AppUser> customers = userRepository.findByRole("ROLE_CUSTOMER");
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/drivers")
    public ResponseEntity<?> getDrivers() {
        try {
            List<AppUser> drivers = userRepository.findByRole("ROLE_DRIVER");
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAdmins() {
        try {
            List<AppUser> admins = userRepository.findByRole("ROLE_ADMIN");
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }

    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody AppUser updatedUser) {
        try {
            AppUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Cập nhật các thông tin khác
            user.setEmail(updatedUser.getEmail());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setFullName(updatedUser.getFullName());
            user.setActive(updatedUser.isActive());

            // Đổi mật khẩu nếu có nhập
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi cập nhật: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username, org.springframework.security.core.Authentication auth) {
        try {
            if (auth != null && auth.getName().equals(username)) {
                return ResponseEntity.badRequest().body("Bạn không thể tự xóa tài khoản của chính mình!");
            }
            
            if (!userRepository.existsByUsername(username)) {
                return ResponseEntity.status(404).body("Không tìm thấy người dùng: " + username);
            }
            
            userRepository.deleteByUsername(username);
            return ResponseEntity.ok("Đã xóa user: " + username);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi xóa: " + e.getMessage());
        }
    }
}
