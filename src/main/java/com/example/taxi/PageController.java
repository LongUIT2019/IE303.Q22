package com.example.taxi;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Autowired
    private FirestoreUserRepository userRepository;

    // Trang dành cho khách hàng gọi xe
    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    // Trang dành cho tài xế nhận chuyến
    @GetMapping("/driver")
    public String driverPage(org.springframework.security.core.Authentication auth, Model model) {
        if (auth != null) {
            model.addAttribute("driverName", auth.getName());
        }
        return "driver";
    }

    // Trang tổng quan cho admin
    @GetMapping("/admin")
    public String adminPage(org.springframework.security.core.Authentication auth, Model model) {
        if (auth != null) {
            model.addAttribute("currentAdmin", auth.getName());
        }
        return "admin";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(Model model) {
        model.addAttribute("users", userRepository.findByRole("ROLE_CUSTOMER"));
        return "admin-users";
    }

    @GetMapping("/admin/drivers")
    public String adminDriversPage(Model model) {
        model.addAttribute("drivers", userRepository.findByRole("ROLE_DRIVER"));
        return "admin-drivers";
    }

    // Trang Đăng nhập hệ thống
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
