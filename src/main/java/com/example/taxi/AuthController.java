package com.example.taxi;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final FirestoreUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(FirestoreUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login/customer")
    public String loginCustomer() {
        return "login_customer";
    }

    @GetMapping("/login/driver")
    public String loginDriver() {
        return "login_driver";
    }

    @GetMapping("/login/admin")
    public String loginAdmin() {
        return "login_admin";
    }

    @GetMapping("/register/customer")
    public String registerCustomerPage() {
        return "register_customer";
    }

    @GetMapping("/register/driver")
    public String registerDriverPage() {
        return "register_driver";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            Model model) {

        // Kiểm tra username trùng
        Optional<AppUser> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại!");
            return role.equals("DRIVER") ? "register_driver" : "register_customer";
        }

        // Kiểm tra email trùng
        Optional<AppUser> existingEmail = userRepository.findByEmail(email);
        if (existingEmail.isPresent()) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return role.equals("DRIVER") ? "register_driver" : "register_customer";
        }

        String targetRole = role.equals("DRIVER") ? "ROLE_DRIVER" : "ROLE_CUSTOMER";
        AppUser newUser = new AppUser(username, passwordEncoder.encode(password), targetRole, email, fullName,
                phoneNumber);
        newUser.setActive(true); // Kích hoạt ngay khi đăng ký

        userRepository.save(newUser);

        // Tự động đăng nhập sau khi đăng ký thành công
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                newUser.getUsername(), null, 
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(targetRole));
        
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authToken);

        // Redirect trực tiếp về trang Dashboard tương ứng
        if (role.equals("DRIVER")) {
            return "redirect:/driver";
        } else {
            return "redirect:/";
        }
    }
}
