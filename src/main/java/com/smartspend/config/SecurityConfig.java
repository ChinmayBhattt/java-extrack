package com.smartspend.config;

import com.smartspend.security.JwtAuthFilter;
import com.smartspend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <p>Strategy:
 * <ul>
 *   <li>Stateless JWT-based auth for REST APIs ({@code /api/**})</li>
 *   <li>Thymeleaf pages ({@code /pages/**}) require auth and redirect to login</li>
 *   <li>CSRF disabled (JWT approach is inherently CSRF-safe for REST; Thymeleaf uses meta-token)</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ── Public endpoints that never need authentication ───────────────────
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/**",           // login, register
        "/",                      // landing / redirect page
        "/login",                 // Thymeleaf login page
        "/register",              // Thymeleaf register page
        "/css/**",                // static resources
        "/js/**",
        "/images/**",
        "/h2-console/**",         // H2 dev console
        "/error"                  // Spring default error page
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API (JWT handles security)
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console iframes
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )

            // Session policy: stateless for /api, default for pages
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )

            // For Thymeleaf pages: redirect to /login on 401/403
            // For API calls (Accept: application/json or /api/**): return 401/403
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String acceptHeader = request.getHeader("Accept");
                    String requestUri   = request.getRequestURI();
                    if (requestUri.startsWith("/api/") ||
                        (acceptHeader != null && acceptHeader.contains("application/json"))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )

            // Custom auth provider
            .authenticationProvider(authenticationProvider())

            // JWT filter runs before Spring's default auth filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
