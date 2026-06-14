package com.example.taxi;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final FirestoreUserRepository userRepository;

    public CustomAuthenticationSuccessHandler(FirestoreUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        Optional<AppUser> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            String role = user.getRole();
            if (role.equals("ROLE_ADMIN")) {
                response.sendRedirect("/admin");
            } else if (role.equals("ROLE_DRIVER")) {
                response.sendRedirect("/driver");
            } else {
                response.sendRedirect("/");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
