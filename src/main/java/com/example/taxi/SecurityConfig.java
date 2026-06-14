package com.example.taxi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final FirestoreUserRepository userRepository;
        private final CustomAuthenticationSuccessHandler successHandler;

        public SecurityConfig(FirestoreUserRepository userRepository, CustomAuthenticationSuccessHandler successHandler) {
                this.userRepository = userRepository;
                this.successHandler = successHandler;
        }

        /**
         * Xác định trang login phù hợp dựa trên Referer header.
         * Ví dụ: nếu user đăng nhập từ /login/driver thì redirect về /login/driver
         */
        private String detectLoginPage(jakarta.servlet.http.HttpServletRequest request) {
                String referer = request.getHeader("Referer");
                if (referer != null) {
                        if (referer.contains("/login/driver")) {
                                return "/login/driver";
                        }
                        if (referer.contains("/login/admin")) {
                                return "/login/admin";
                        }
                }
                return "/login/customer";
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests((requests) -> requests
                                                .requestMatchers("/images/**", "/css/**", "/js/**", "/register/**",
                                                                "/verify/**", "/login/**")
                                                .permitAll()
                                                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/ml/predict/**").hasAnyRole("ADMIN", "DRIVER")
                                                .requestMatchers("/driver/**").hasRole("DRIVER")
                                                .requestMatchers("/").hasRole("CUSTOMER")
                                                .requestMatchers("/history", "/api/rides/**", "/api/favorites/**")
                                                .hasAnyRole("CUSTOMER", "DRIVER", "ADMIN")
                                                .anyRequest().authenticated())

                                // Xử lý khi chưa đăng nhập (401) hoặc không có quyền (403)
                                .exceptionHandling(handling -> handling
                                                // Chưa đăng nhập → redirect đến login tương ứng
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        String uri = request.getRequestURI();
                                                        if (uri.startsWith("/driver")) {
                                                                response.sendRedirect("/login/driver");
                                                        } else if (uri.startsWith("/admin")) {
                                                                response.sendRedirect("/login/admin");
                                                        } else {
                                                                response.sendRedirect("/login/customer");
                                                        }
                                                })
                                                // Đã đăng nhập nhưng sai role → redirect về trang đúng role
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        var auth = request.getUserPrincipal();
                                                        if (auth != null) {
                                                                String username = auth.getName();
                                                                Optional<AppUser> userOpt = userRepository
                                                                                .findByUsername(username);
                                                                if (userOpt.isPresent()) {
                                                                        String role = userOpt.get().getRole();
                                                                        if (role.equals("ROLE_ADMIN")) {
                                                                                response.sendRedirect("/admin");
                                                                        } else if (role.equals("ROLE_DRIVER")) {
                                                                                response.sendRedirect("/driver");
                                                                        } else {
                                                                                response.sendRedirect("/");
                                                                        }
                                                                        return;
                                                                }
                                                        }
                                                        response.sendRedirect("/login/customer");
                                                }))

                                .formLogin((form) -> form
                                                .loginPage("/login/customer")
                                                .loginProcessingUrl("/login")
                                                .failureHandler((request, response, exception) -> {
                                                        // Sai mật khẩu → redirect về trang login tương ứng
                                                        String loginPage = detectLoginPage(request);
                                                        response.sendRedirect(loginPage + "?error");
                                                })
                                                .successHandler(successHandler)
                                                .permitAll())

                                .logout((logout) -> logout
                                                .logoutRequestMatcher(
                                                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                                                                "/logout"))
                                                .logoutSuccessHandler((request, response, authentication) -> {
                                                        // Redirect về trang login đúng role sau khi logout
                                                        if (authentication != null
                                                                        && authentication.getPrincipal() != null) {
                                                                String username = authentication.getName();
                                                                Optional<AppUser> userOpt = userRepository
                                                                                .findByUsername(username);
                                                                if (userOpt.isPresent()) {
                                                                        String role = userOpt.get().getRole();
                                                                        if (role.equals("ROLE_ADMIN")) {
                                                                                response.sendRedirect(
                                                                                                "/login/admin?logout");
                                                                                return;
                                                                        } else if (role.equals("ROLE_DRIVER")) {
                                                                                response.sendRedirect(
                                                                                                "/login/driver?logout");
                                                                                return;
                                                                        }
                                                                }
                                                        }
                                                        response.sendRedirect("/login/customer?logout");
                                                })
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                .csrf(csrf -> csrf.disable());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
}
