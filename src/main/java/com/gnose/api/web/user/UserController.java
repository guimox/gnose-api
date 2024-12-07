package com.gnose.api.web.user;

import com.gnose.api.dto.user.LoginRequest;
import com.gnose.api.dto.user.PasswordResetConfirmRequest;
import com.gnose.api.dto.user.PasswordResetRequest;
import com.gnose.api.model.UserGnose;
import com.gnose.api.security.JwtTokenUtil;
import com.gnose.api.web.email.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, EmailService emailService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserGnose user) {
        try {
            UserGnose registeredUser = userService.registerNewUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("externalId", registeredUser.getExternalId());
            response.put("username", registeredUser.getUsername());
            response.put("email", registeredUser.getEmail());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            UserGnose user = userService.findByUsername(loginRequest.getUsername());

            String token = jwtTokenUtil.generateToken(user.getUsername(), user.getAuthSalt());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("externalId", user.getExternalId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        UserGnose currentUser = userService.getCurrentUser();
        userService.regenerateAuthSalt(currentUser);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest resetRequest) {
        try {
            UserGnose user = userService.findByEmail(resetRequest.getEmail());

            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userService.saveUser(user);

            String resetLink = resetRequest.getResetUrl() + "?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

            return ResponseEntity.ok("Password reset link sent to your email");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Email not found");
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest resetConfirmRequest) {
        try {
            userService.resetPassword(resetConfirmRequest.getToken(), resetConfirmRequest.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }
    }

}