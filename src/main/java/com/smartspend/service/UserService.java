package com.smartspend.service;

import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.entity.User;
import com.smartspend.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Manages user profile reads and updates (name and salary).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Read Profile ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile() {
        return UserProfileDTO.from(currentUser());
    }

    // ── Update Profile ────────────────────────────────────────────────────

    @Transactional
    public UserProfileDTO updateProfile(UpdateProfileRequest request) {
        User user = currentUser();

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getSalary() != null) {
            user.setSalary(request.getSalary());
        }
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        user = userRepository.save(user);
        return UserProfileDTO.from(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── Inner DTOs ────────────────────────────────────────────────────────

    @Data
    @Builder
    public static class UserProfileDTO {
        private Long       id;
        private String     name;
        private String     email;
        private BigDecimal salary;

        public static UserProfileDTO from(User user) {
            return UserProfileDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .salary(user.getSalary())
                    .build();
        }
    }

    @Data
    public static class UpdateProfileRequest {
        private String     name;
        private BigDecimal salary;
        private String     newPassword;
    }
}
