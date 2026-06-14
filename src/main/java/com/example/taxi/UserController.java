package com.example.taxi;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class UserController {

    private final FirestoreUserRepository userRepository;
    private final FirestoreService firestoreService;

    public UserController(FirestoreUserRepository userRepository, FirestoreService firestoreService) {
        this.userRepository = userRepository;
        this.firestoreService = firestoreService;
    }

    @GetMapping("/history")
    public String viewHistory(Authentication auth, Model model) {
        if (auth == null)
            return "redirect:/login";
        Optional<AppUser> userOpt = userRepository.findByUsername(auth.getName());
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            try {
                if (user.getRole().equals("ROLE_DRIVER")) {
                    model.addAttribute("rides", firestoreService.findRidesByDriverId(user.getUsername()));
                } else {
                    model.addAttribute("rides", firestoreService.findRidesByCustomerName(user.getUsername()));
                }
            } catch (Exception e) {
                model.addAttribute("rides", java.util.Collections.emptyList());
                model.addAttribute("error", "Không thể tải lịch sử: " + e.getMessage());
            }
        }
        return "history";
    }
}
