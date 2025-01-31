package com.gnose.api.web.user;

import com.gnose.api.dto.user.RegisterRequest;
import com.gnose.api.model.Token;
import com.gnose.api.model.User;
import com.gnose.api.jwt.JwtTokenUtil;
import com.gnose.api.web.email.EmailService;
import com.gnose.api.web.token.TokenRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil,
            EmailService emailService,
            AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User newUser = new User(registerRequest.getName(), registerRequest.getEmail(), hashedPassword);
        userRepository.save(newUser);

        String confirmationToken = jwtTokenUtil.generateConfirmationToken(registerRequest.getEmail());

        String confirmUrl = "http://localhost:8081" + "/api/auth/register/confirm";
        String confirmationLink = confirmUrl + "?token=" + confirmationToken;

        emailService.sendConfirmationEmail(registerRequest.getEmail(), "link");
    }

//    @Transactional
//    public void confirmUserRegistration(String token) {
//        User user = userRepository.findByConfirmationToken(token)
//                .orElseThrow(() -> new RuntimeException("Invalid confirmation token."));
//        if (user.getConfirmationExpiry().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Confirmation token has expired.");
//        }
//
//        user.setEnabled(true);
//        user.setConfirmationToken(null);
//        user.setConfirmationExpiry(null);
//        userRepository.save(user);
//    }

//    @Transactional
//    public Map<String, Object> authenticateUser(LoginRequest loginRequest) {
//        User user = loginRequest.getLogin().contains("@")
//                ? userRepository.findByEmail(loginRequest.getLogin())
//                .orElseThrow(() -> new RuntimeException("User not found."))
//                : userRepository.findByUsername(loginRequest.getLogin())
//                .orElseThrow(() -> new RuntimeException("User not found."));
//
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
//        );
//
//        if (!authentication.isAuthenticated()) {
//            throw new RuntimeException("Invalid login credentials.");
//        }
//
//        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getAuthSalt());
//        Map<String, Object> response = new HashMap<>();
//        response.put("token", token);
//        response.put("username", user.getUsername());
//        response.put("externalId", user.getExternalId());
//        return response;
//    }

    @Transactional
    public void logoutCurrentUser() {
        User currentUser = getCurrentUser();
        currentUser.setAuthSalt(UUID.randomUUID().toString());
        userRepository.save(currentUser);
    }

//    @Transactional
//    public void handlePasswordResetRequest(PasswordResetRequest resetRequest) {
//        User user = userRepository.findByEmail(resetRequest.getEmail())
//                .orElseThrow(() -> new RuntimeException("Email not found."));
//
//        user.setPasswordResetToken(UUID.randomUUID().toString());
//        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1)); // 1-hour expiry
//        userRepository.save(user);
//
//        String resetLink = resetRequest.getResetUrl() + "?token=" + user.getPasswordResetToken();
//        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
//    }
//
//    @Transactional
//    public void confirmPasswordReset(PasswordResetConfirmRequest resetConfirmRequest) {
//        User user = userRepository.findByPasswordResetToken(resetConfirmRequest.getToken())
//                .orElseThrow(() -> new RuntimeException("Invalid reset token."));
//        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Reset token has expired.");
//        }
//
//        user.setPassword(passwordEncoder.encode(resetConfirmRequest.getNewPassword()));
//        user.setPasswordResetToken(null);
//        user.setPasswordResetExpiry(null);
//        userRepository.save(user);
//    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No authenticated user.");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found."));
    }
}
