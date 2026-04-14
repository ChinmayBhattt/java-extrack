package com.smartspend.controller;

import com.smartspend.dto.auth.AuthResponse;
import com.smartspend.dto.auth.LoginRequest;
import com.smartspend.dto.auth.RegisterRequest;
import com.smartspend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication.
 * Endpoints: POST /api/auth/register, POST /api/auth/login, POST /api/auth/logout
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Register a new user account. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletResponse httpResponse
    ) {
        AuthResponse result = authService.register(request);
        setJwtCookie(httpResponse, result.getToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /** Authenticate and receive a JWT. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse httpResponse
    ) {
        AuthResponse result = authService.login(request);
        setJwtCookie(httpResponse, result.getToken());
        return ResponseEntity.ok(result);
    }

    /** Clear the JWT cookie to log out. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse httpResponse) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpResponse.addCookie(cookie);
        return ResponseEntity.noContent().build();
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);       // Not accessible from JS (XSS protection)
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        response.addCookie(cookie);
    }
}
