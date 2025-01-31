package com.gnose.api.web.user;

import com.gnose.api.dto.user.AuthRequest;
import com.gnose.api.dto.user.AuthResponse;
import com.gnose.api.dto.user.PasswordResetRequest;
import com.gnose.api.dto.user.RegisterRequest;
import com.gnose.api.jwt.JwtTokenUtil;
import com.gnose.api.model.User;
import com.gnose.api.util.AuthUtils;
import com.gnose.api.web.email.EmailService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final AuthUtils authUtils;

    public UserService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            EmailService emailService,
            AuthUtils authUtils) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.emailService = emailService;
        this.authUtils = authUtils;
    }

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        String userEmail = registerRequest.getEmail();

        if (!authUtils.isValidEmailFormat(userEmail)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (userRepository.existsByEmail(userEmail)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String hashedPassword = authUtils.hashPassword(registerRequest.getPassword());
        User newUser = new User(registerRequest.getName(), userEmail, hashedPassword);
        userRepository.save(newUser);

        String confirmationToken = jwtTokenUtil.generateConfirmationToken(userEmail);
        String confirmationLink = "http://localhost:8081/users/register/confirm?token=" + confirmationToken;

        emailService.sendConfirmationEmail(userEmail, confirmationLink);
    }

    @Transactional
    public void handlePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = jwtTokenUtil.generatePasswordResetToken(user.getEmail());
        String resetLink = "http://localhost:8081/users/reset-password?token=" + resetToken;

        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

    @Transactional
    public void logoutCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAuthSalt(UUID.randomUUID().toString()); // Invalidate existing JWTs
        userRepository.save(user);
    }

    @Transactional
    public void confirmUserRegistration(String token) {
        String userEmail = jwtTokenUtil.getUserEmailFromToken(token);
        if (!jwtTokenUtil.validateConfirmationToken(token, userEmail) ||
                authUtils.isTokenExpired(jwtTokenUtil.getExpirationDateFromTokenConfirmation(token))) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation token."));

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetRequest request, String authHeaderToken) {
        String email = jwtTokenUtil.getUserEmailFromToken(request.getEmail());

        if (!jwtTokenUtil.validatePasswordResetToken(authHeaderToken, email) ||
                authUtils.isTokenExpired(jwtTokenUtil.getExpirationDateFromToken(authHeaderToken))) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(authUtils.hashPassword(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is not enabled. Please verify your email.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            userRepository.save(user);

            String jwt = jwtTokenUtil.generateToken(user.getEmail(), user.getAuthSalt());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail(), user.getAuthSalt());

            return new AuthResponse(jwt, refreshToken, user.getEmail());

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid credentials.");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtTokenUtil.getUserEmailFromToken(refreshToken);
        String salt = jwtTokenUtil.getSaltFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getAuthSalt().equals(salt)) {
            throw new RuntimeException("Invalid refresh token.");
        }

        String newAccessToken = jwtTokenUtil.generateToken(email, salt);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(email, salt);

        return new AuthResponse(newAccessToken, newRefreshToken, email);
    }
}
