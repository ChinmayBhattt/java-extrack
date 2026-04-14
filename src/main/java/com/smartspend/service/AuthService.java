package com.smartspend.service;

import com.smartspend.dto.auth.AuthResponse;
import com.smartspend.dto.auth.LoginRequest;
import com.smartspend.dto.auth.RegisterRequest;
import com.smartspend.exception.EmailAlreadyExistsException;
import com.smartspend.model.entity.User;
import com.smartspend.repository.UserRepository;
import com.smartspend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and authentication (login).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ── Registration ──────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Guard: email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "An account with email '" + request.getEmail() + "' already exists."
            );
        }

        // Build and persist the new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .salary(request.getSalary())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        // Issue JWT immediately (auto-login after register)
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .salary(user.getSalary())
                .message("Registration successful! Welcome to SmartSpend.")
                .build();
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Delegate credential validation to Spring Security's AuthenticationManager
        // (throws BadCredentialsException if invalid — handled by GlobalExceptionHandler)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();    // Already verified above

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .salary(user.getSalary())
                .message("Login successful!")
                .build();
    }
}
