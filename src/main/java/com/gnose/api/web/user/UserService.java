package com.gnose.api.web.user;

import com.gnose.api.model.UserGnose;
import com.gnose.api.security.JwtTokenUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Transactional
    public UserGnose registerNewUser(UserGnose userGnose) {
        // Check if username exists
        if (userRepository.existsByUsername(userGnose.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email exists
        if (userRepository.existsByEmail(userGnose.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Encode password
        userGnose.setPassword(passwordEncoder.encode(userGnose.getPassword()));

        // Generate initial auth salt
        userGnose.setAuthSalt(UUID.randomUUID().toString());

        // Save user
        return userRepository.save(userGnose);
    }

    @Transactional(readOnly = true)
    public UserGnose findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserGnose findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void regenerateAuthSalt(UserGnose user) {
        user.setAuthSalt(UUID.randomUUID().toString());
        userRepository.save(user);
    }

    @Transactional
    public void saveUser(UserGnose user) {
        userRepository.save(user);
    }

    @Transactional
    public UserGnose getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user");
        }

        String username = authentication.getName();
        return findByUsername(username);
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        // Find user by reset token and check expiry
        UserGnose user = userRepository.findByPasswordResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check if token is expired
        if (user.getPasswordResetExpiry() == null ||
                user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear reset token and expiry
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);

        // Save updated user
        userRepository.save(user);
    }
}