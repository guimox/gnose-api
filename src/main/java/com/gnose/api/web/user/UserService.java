package com.gnose.api.web.user;

import com.gnose.api.dto.user.LoginRequest;
import com.gnose.api.dto.user.PasswordResetConfirmRequest;
import com.gnose.api.dto.user.PasswordResetRequest;
import com.gnose.api.model.UserGnose;
import com.gnose.api.security.JwtTokenUtil;
import com.gnose.api.web.email.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil,
            EmailService emailService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void registerUser(UserGnose user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // Initialize user with default values
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // Account inactive
        user.setAuthSalt(UUID.randomUUID().toString());
        user.setConfirmationToken(UUID.randomUUID().toString());
        user.setConfirmationExpiry(LocalDateTime.now().plusHours(24)); // Token valid for 24h
        userRepository.save(user);

        // Send confirmation email
        String confirmUrl = "http://localhost:8081" + "/api/auth/register/confirm";
        String confirmationLink = confirmUrl + "?token=" + user.getConfirmationToken();
        emailService.sendConfirmationEmail(user.getEmail(), confirmationLink);
    }

    @Transactional
    public void confirmUserRegistration(String token) {
        UserGnose user = userRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation token."));
        if (user.getConfirmationExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Confirmation token has expired.");
        }

        user.setEnabled(true);
        user.setConfirmationToken(null);
        user.setConfirmationExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public Map<String, Object> authenticateUser(LoginRequest loginRequest) {
        UserGnose user = loginRequest.getLogin().contains("@")
                ? userRepository.findByEmail(loginRequest.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found."))
                : userRepository.findByUsername(loginRequest.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("Invalid login credentials.");
        }

        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getAuthSalt());
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("externalId", user.getExternalId());
        return response;
    }

    @Transactional
    public void logoutCurrentUser() {
        UserGnose currentUser = getCurrentUser();
        currentUser.setAuthSalt(UUID.randomUUID().toString());
        userRepository.save(currentUser);
    }

    @Transactional
    public void handlePasswordResetRequest(PasswordResetRequest resetRequest) {
        UserGnose user = userRepository.findByEmail(resetRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found."));

        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1)); // 1-hour expiry
        userRepository.save(user);

        String resetLink = resetRequest.getResetUrl() + "?token=" + user.getPasswordResetToken();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest resetConfirmRequest) {
        UserGnose user = userRepository.findByPasswordResetToken(resetConfirmRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token."));
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(resetConfirmRequest.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserGnose getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user.");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
    }
}
