package com.gnose.api.web.user;

import com.gnose.api.dto.user.request.AuthRequestDTO;
import com.gnose.api.dto.user.response.AuthResponseDTO;
import com.gnose.api.dto.user.request.PasswordResetRequestDTO;
import com.gnose.api.dto.user.request.RegisterRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            userService.registerUser(registerRequestDTO);
            return ResponseEntity.ok("User registration initiated. Please check your email to confirm.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> handlePasswordReset(@RequestParam String email) {
        try {
            userService.handlePasswordReset(email);
            return ResponseEntity.ok("Password reset link has been sent to your email.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/register/confirm")
    public ResponseEntity<String> confirmUserRegistration(@RequestParam String token) {
        try {
            userService.confirmUserRegistration(token);
            return ResponseEntity.ok("User registration confirmed.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/protected")
    public String protect() {
        return "Protected route";
    }

    @PostMapping("/reset-password/confirm")
    public ResponseEntity<String> confirmPasswordReset(@RequestBody PasswordResetRequestDTO request,
                                                       @RequestHeader("Authorization") String authHeaderToken) {
        try {
            userService.confirmPasswordReset(request, authHeaderToken);
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            AuthResponseDTO authResponseDTO = userService.authenticateUser(authRequestDTO);
            return ResponseEntity.ok(authResponseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        try {
            AuthResponseDTO authResponseDTO = userService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.logoutCurrentUser(userDetails.getUsername());
            return ResponseEntity.ok("User logged out successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
