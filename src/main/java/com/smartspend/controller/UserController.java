package com.smartspend.controller;

import com.smartspend.service.UserService;
import com.smartspend.service.UserService.UpdateProfileRequest;
import com.smartspend.service.UserService.UserProfileDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** GET /api/users/me – get the authenticated user's profile. */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    /** PUT /api/users/me – update name, salary, or password. */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}
