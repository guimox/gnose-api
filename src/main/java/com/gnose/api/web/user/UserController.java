package com.gnose.api.web.user;

import com.gnose.api.dto.user.LoginRequest;
import com.gnose.api.dto.user.PasswordResetConfirmRequest;
import com.gnose.api.dto.user.PasswordResetRequest;
import com.gnose.api.dto.user.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.registerUser(registerRequest);
            return ResponseEntity.ok("Registration successful! Check your email to confirm your account.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/register/confirm")
    public ResponseEntity<?> confirmRegistration(@RequestParam String token) {
        try {
            userService.confirmUserRegistration(token);
            return ResponseEntity.ok("User confirmed successfully. You can now log in.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(userService.authenticateUser(loginRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        userService.logoutCurrentUser();
        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest resetRequest) {
        try {
            userService.handlePasswordResetRequest(resetRequest);
            return ResponseEntity.ok("Password reset link sent to your email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest resetConfirmRequest) {
        try {
            userService.confirmPasswordReset(resetConfirmRequest);
            return ResponseEntity.ok("Password reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
